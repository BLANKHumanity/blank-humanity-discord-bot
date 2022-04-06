package com.blank.humanity.discordbot.services;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.database.NftTokenTradeDao;
import com.blank.humanity.discordbot.wallet.entities.etherscan.trade.NftTokenTrade;

@Service
public class NftSalesService {

    @Autowired
    private List<NftSalesRetriever> nftSalesRetriever;

    @Autowired
    private NftTokenTradeDao nftTokenTradeDao;

    public Stream<NftTokenTrade> retrieveNewerSales(String tokenContract) {
        return nftSalesRetriever
            .stream()
            .parallel()
            .flatMap(retriever -> {
                String retrieverId = retriever.getRetrieverId();

                long lastKnownBlock = nftTokenTradeDao
                    .findLastKnownBlock(tokenContract, retrieverId)
                    .orElse(0l) + 1;

                return retriever
                    .retrieveNewSales(lastKnownBlock, tokenContract)
                    .map(trade -> trade.setRetrieverId(retrieverId));
            });
    }

}
