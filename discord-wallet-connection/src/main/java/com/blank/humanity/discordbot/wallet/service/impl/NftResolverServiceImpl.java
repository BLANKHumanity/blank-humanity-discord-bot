package com.blank.humanity.discordbot.wallet.service.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.DefaultGasProvider;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.smartcontracts.InitializerSmartContract;
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
@Service
public class NftResolverServiceImpl implements NftResolverService {

    @Setter(onMethod = @__({ @Autowired }))
    private DiscordWalletService discordWalletService;

    @Setter(onMethod = @__({ @Autowired }))
    private Web3j web3;

    @Setter(onMethod = @__({ @Autowired }))
    private EtherscanApiService etherscanApiService;

    @Setter(onMethod = @__({ @Autowired }))
    private NftOwnerEntityDao nftOwnerEntityDao;
    
    @Setter(onMethod = @__({ @Resource }))
    private NftResolverService self;
    

    @Transactional
    @Scheduled(fixedDelay = 60000l)
    public void lookupNftTransfers() {
        nftOwnerEntityDao.findNftContracts().stream()
    }

    @Override
    @Cacheable(cacheNames = "nftOwners", key = "#root.args[0].concat('_').concat(#root.args[1])")
    public CompletableFuture<String> findOwner(String nftContractAddress,
        long nftId) {
        InitializerSmartContract smartContract = InitializerSmartContract
            .load(nftContractAddress, web3, (Credentials) null,
                new DefaultGasProvider());

        return smartContract.ownerOf(BigInteger.valueOf(nftId)).sendAsync();
    }

    @Override
    public CompletableFuture<Optional<BlankUser>> findBlankUserOwner(
        String nftContractAddress, long nftId) {
        return self
            .findOwner(nftContractAddress, nftId)
            .thenApply(
                address -> discordWalletService.findUserByWallet(address));
    }

    @Override
    @Cacheable(cacheNames = "nftOwners")
    public List<Long> findOwnedNFTs(String nftContractAddress, Address owner) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> findOwnedNFTs(String nftContractAddress,
        BlankUser owner) {
        // TODO Auto-generated method stub
        return null;
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
