package com.blank.humanity.discordbot.wallet.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.config.NftResolverConfig;
import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;
import com.blank.humanity.discordbot.wallet.entities.NftOwnerEntity;
import com.blank.humanity.discordbot.wallet.entities.etherscan.trade.NftTokenTransferEvent;
import com.blank.humanity.discordbot.wallet.entities.etherscan.trade.NftTokenTransferEventsResponse;
import com.blank.humanity.discordbot.wallet.persistence.NftOwnerEntityDao;
import com.blank.humanity.discordbot.wallet.service.DiscordWalletService;
import com.blank.humanity.discordbot.wallet.service.EtherscanApiService;
import com.blank.humanity.discordbot.wallet.service.NftResolverService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NftResolverServiceImpl implements NftResolverService {

    @Setter(onMethod = @__({ @Autowired }))
    private DiscordWalletService discordWalletService;

    @Setter(onMethod = @__({ @Autowired }))
    private Web3j web3;

    @Setter(onMethod = @__({ @Autowired }))
    private EtherscanApiService etherscanApiService;

    @Setter(onMethod = @__({ @Autowired }))
    private NftOwnerEntityDao nftOwnerEntityDao;

    @Setter(onMethod = @__({ @Autowired }))
    private NftResolverConfig nftResolverConfig;

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void lookupNftTransfers() {
        nftResolverConfig
            .getContracts()
            .stream()
            .forEach(this::lookupNftContractsTransfers);
    }

    private void lookupNftContractsTransfers(String contract) {
        long lastTransferBlock = nftOwnerEntityDao
            .findLastKnownTransferBlock(contract)
            .orElse(0L);
        log.info("Looking up Transfers for Contract " + contract);
        etherscanApiService
            .fetchNftTokenTransferEvents(contract, lastTransferBlock)
            .map(NftTokenTransferEventsResponse::getTokenTransferEvents)
            .flatMap(List::stream)
            .sorted(Comparator.comparing(NftTokenTransferEvent::getBlockNumber))
            .limit(100)
            .forEach(transfer -> nftOwnerEntityDao
                .findByNftContractAndNftTokenId(contract, transfer.getTokenID())
                .ifPresentOrElse(nftEntity -> {
                    if (nftEntity.getTransferBlock() < transfer
                        .getBlockNumber()) {
                        nftEntity.setOwner(transfer.getTo().toLowerCase());
                        nftEntity.setTransferBlock(transfer.getBlockNumber());
                        nftOwnerEntityDao.saveAndFlush(nftEntity);
                    }
                }, () -> {
                    NftOwnerEntity nftEntity = NftOwnerEntity
                        .builder()
                        .nftContract(contract)
                        .nftTokenId(transfer.getTokenID())
                        .transferBlock(lastTransferBlock)
                        .owner(transfer.getTo())
                        .build();
                    log
                        .info("Saving new Token " + transfer.getTokenID()
                            + " for Contract " + contract);
                    nftOwnerEntityDao.saveAndFlush(nftEntity);
                }));
    }

    @Override
    public Optional<String> findOwner(String nftContractAddress,
        long nftId) {
        return nftOwnerEntityDao
            .findByNftContractAndNftTokenId(nftContractAddress, nftId)
            .map(NftOwnerEntity::getOwner);
    }

    @Override
    public Optional<BlankUser> findBlankUserOwner(
        String nftContractAddress, long nftId) {
        return findOwner(nftContractAddress, nftId)
            .flatMap(address -> discordWalletService.findUserByWallet(address));
    }

    @Override
    public List<Long> findOwnedNFTs(String nftContractAddress,
        String... ownerAddresses) {
        List<String> lowercaseAddresses = Arrays
            .stream(ownerAddresses)
            .map(String::toLowerCase)
            .toList();
        return nftOwnerEntityDao
            .findByNftContractAndOwnerIn(nftContractAddress, lowercaseAddresses)
            .stream()
            .map(NftOwnerEntity::getNftTokenId)
            .toList();
    }

    @Override
    public List<Long> findOwnedNFTs(String nftContractAddress,
        BlankUser owner) {
        String[] ownerAddresses = discordWalletService
            .getWallets(owner)
            .stream()
            .map(DiscordVerifiedWallet::getWalletAddress)
            .toArray(size -> new String[size]);
        return findOwnedNFTs(nftContractAddress, ownerAddresses);
    }

    @Override
    @Cacheable(cacheNames = "nftImages", key = "#root.args[0].concat('_').concat(#root.args[1])", unless = "#result==null")
    public Optional<byte[]> fetchNftImage(String initializerAddress,
        long initializerId) {
        return fetchImage("QmfHWVaX3NDvamvVQKH5wYWSL2w8tMSshvhngwpKSMduNp",
            initializerId);
    }

    public Optional<byte[]> fetchImage(String ipfsFolder, long tokenId) {
        RestTemplate rest = new RestTemplate();
        ResponseEntity<IPFSMetadata> metadata;
        try {
            metadata = rest
                .getForEntity(
                    "https://cloudflare-ipfs.com/ipfs/" + ipfsFolder + "/"
                        + tokenId,
                    IPFSMetadata.class);
        } catch (Exception e) {
            return Optional.empty();
        }

        if (metadata.getStatusCode() != HttpStatus.OK) {
            return Optional.empty();
        }

        IPFSMetadata ipfsMetadata = metadata.getBody();

        if (ipfsMetadata == null) {
            return Optional.empty();
        }

        String cid = ipfsMetadata
            .getImage()
            .substring("ipfs://".length());

        return Optional
            .ofNullable(rest
                .getForObject("https://cloudflare-ipfs.com/ipfs/" + cid,
                    byte[].class));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    protected static class IPFSMetadata {

        private String image;

    }

}
