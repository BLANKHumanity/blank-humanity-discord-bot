package com.blank.humanity.discordbot.services.etherscan;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.database.NftTokenTradeDao;
import com.blank.humanity.discordbot.entities.etherscan.logs.TransactionLogEntry;
import com.blank.humanity.discordbot.entities.etherscan.logs.TransactionLogsRequest;
import com.blank.humanity.discordbot.entities.etherscan.logs.TransactionLogsResponse;
import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTrade;
import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTransferEvent;
import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTransferEventsResponse;

@Service
public class OpenSeaSalesRetrieverService implements NftSalesRetriever {

    @Autowired
    private NftTokenTradeDao nftTokenTradeDao;

    @Autowired
    private EtherscanApiService etherscanApiService;

    /**
     * Retrieves all NftTrades between blockStart and blockEnd of a particular
     * token contract
     * 
     * @param tokenContract
     * @param blockStart
     * @return
     */
    public Stream<NftTokenTrade> retrieveNftTrades(String tokenContract,
        long blockStart) {
        return etherscanApiService
            .fetchNftTokenTransferEvents(tokenContract, blockStart)
            .sequential()
            .map(NftTokenTransferEventsResponse::getTokenTransferEvents)
            .flatMap(List::stream)
            .filter(event -> !event.getFrom().substring(2).matches("0{40}"))
            .flatMap(this::mapTransferEventToTokenTrade)
            .filter(Objects::nonNull);
    }

    private Stream<NftTokenTrade> mapTransferEventToTokenTrade(
        NftTokenTransferEvent transferEvent) {
        TransactionLogsRequest logsRequest = new TransactionLogsRequest()
            .address("0x7f268357a8c2552623316e2562d90e642bb538e5")
            .topic0(
                "0xc4109843e0b7d514e4c093114b863f8e7d8d9a458c372cd51bfe526b588006c9")
            .topic1(padding(transferEvent.getFrom()))
            .topic2(padding(transferEvent.getFrom()))
            .topic1_2_opr("or")
            .fromBlock(transferEvent.getBlockNumber())
            .toBlock(transferEvent.getBlockNumber());

        return etherscanApiService
            .fetchTransactionLogs(logsRequest)
            .map(TransactionLogsResponse::getTransactionLogs)
            .flatMap(List::stream)
            .filter(log -> log
                .getTransactionHash()
                .equalsIgnoreCase(transferEvent.getHash()))
            .filter(doTradersMatch(transferEvent.getFrom().substring(2),
                transferEvent.getTo().substring(2)))
            .map(log -> NftTokenTrade.fromLogEntry(log, transferEvent));
    }

    private String padding(String unpadded) {
        String padding = "0000000000000000000000000000000000000000000000000000000000000000";
        unpadded = unpadded.startsWith("0x") ? unpadded.substring(2) : unpadded;
        return "0x" + padding.substring(unpadded.length()) + unpadded;
    }

    private Predicate<? super TransactionLogEntry> doTradersMatch(String from,
        String to) {
        return log -> {
            String taker = log.getTopics().get(1);
            String maker = log.getTopics().get(2);
            if (log.getDataPoint(0).matches("0{64}")) {
                // BuyHash empty => Taker is Seller
                // Maker is Buyer
                return taker.contains(from) && maker.contains(to);
            } else {
                // BuyHash exists => Maker is Seller
                // Taker is Buyer
                return maker.contains(from) && taker.contains(to);
            }
        };
    }

    @Override
    public Stream<NftTokenTrade> retrieveNewSales(long lastBlock,
        String tokenContract) {
        return retrieveNftTrades(tokenContract, lastBlock)
            .map(trade -> {
                ExampleMatcher matcher = ExampleMatcher
                    .matching()
                    .withIgnorePaths("id");
                if (nftTokenTradeDao.exists(Example.of(trade, matcher))) {
                    return null;
                }
                return nftTokenTradeDao.saveAndFlush(trade);
            })
            .filter(Objects::nonNull);
    }

    @Override
    public String getRetrieverId() {
        return "opensea";
    }

}
