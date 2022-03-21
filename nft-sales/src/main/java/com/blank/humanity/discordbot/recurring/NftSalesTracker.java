package com.blank.humanity.discordbot.recurring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.web3j.ens.EnsResolver;

import com.blank.humanity.discordbot.config.NftSalesTrackerConfig;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTrade;
import com.blank.humanity.discordbot.services.etherscan.NftSalesService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

@Slf4j
@Component
public class NftSalesTracker {

    @Autowired
    private JDA jda;

    @Autowired
    private NftSalesTrackerConfig salesTrackerConfig;

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private NftSalesService nftSalesService;

    @Autowired
    private EnsResolver ens;

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void checkNftSales() {
        log.info("Checking NFT-Sales");
        salesTrackerConfig
            .getContractWatchList()
            .entrySet()
            .stream()
            .flatMap(entry -> nftSalesService
                .retrieveNewerSales(entry.getKey())
                .map(trade -> Pair.of(entry.getValue(), trade)))
            .forEach(this::sendNftTradeNotification);
    }

    public void sendNftTradeNotification(
        Pair<String, NftTokenTrade> tradePair) {
        String tokenName = tradePair.getFirst();
        NftTokenTrade trade = tradePair.getSecond();

        EmbedBuilder builder = new EmbedBuilder();

        BigDecimal price = new BigDecimal(new BigInteger(trade.getPrice(), 16))
            .divide(BigDecimal.valueOf(10).pow(18));

        byte[] imageData = fetchImage(trade.getTokenId());

        TextChannel channel = jda
            .getGuildById(commandConfig.getGuildId())
            .getTextChannelById(salesTrackerConfig.getSalesChannel());

        String from = resolveAddress(trade.getFromAddress());
        String to = resolveAddress(trade.getToAddress());

        String openseaUrl = "https://opensea.io/assets/"
            + trade.getNftContract() + "/" + trade.getTokenId();
        String etherscanTransaction = "https://etherscan.io/tx/"
            + trade.getTransactionHash();

        builder
            .setTitle(
                tokenName + " Sold -- " + price.toString() + "ETH",
                etherscanTransaction)
            .setDescription("[" + tokenName + " " + trade.getTokenId() + "]("
                + openseaUrl + ")")
            .setImage("attachment://nft_" + trade.getId() + "_"
                + trade.getTokenId() + ".png")
            .addField("Amount", price.toString() + "ETH", true)
            .addField("From",
                "[" + from + "](https://opensea.io/" + trade.getFromAddress()
                    + ")",
                true)
            .addField("To",
                "[" + to + "](https://opensea.io/" + trade.getToAddress() + ")",
                true);

        channel
            .sendMessageEmbeds(builder.build())
            .addFile(imageData,
                "nft_" + trade.getId() + "_" + trade.getTokenId()
                    + ".png")
            .queue();
    }

    private String resolveAddress(String address) {
        try {
            String ensName = ens.reverseResolve(address);

            if (ens.resolve(ensName).equalsIgnoreCase(address)) {
                return ensName;
            }
        } catch (Exception e) {
            log.debug("ENS resolution failed", e);
        }
        return address;
    }

    public byte[] fetchImage(long tokenId) {
        RestTemplate rest = new RestTemplate();
        ResponseEntity<IPFSMetadata> metadata;
        try {
            metadata = rest
                .getForEntity(
                    "https://ipfs.io/ipfs/QmfHWVaX3NDvamvVQKH5wYWSL2w8tMSshvhngwpKSMduNp/"
                        + tokenId,
                    IPFSMetadata.class);
        } catch (Exception e) {
            return new byte[0];
        }

        if (metadata.getStatusCode() != HttpStatus.OK) {
            return new byte[0];
        }

        IPFSMetadata ipfsMetadata = metadata.getBody();

        if (ipfsMetadata == null) {
            return new byte[0];
        }

        String cid = ipfsMetadata
            .getImage()
            .substring("ipfs://".length());

        return rest.getForObject("https://ipfs.io/ipfs/" + cid, byte[].class);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    protected static class IPFSMetadata {

        private String image;

    }

}
