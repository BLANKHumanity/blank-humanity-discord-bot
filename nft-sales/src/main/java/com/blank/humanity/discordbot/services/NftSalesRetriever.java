package com.blank.humanity.discordbot.services;

import java.util.stream.Stream;

import com.blank.humanity.discordbot.wallet.entities.etherscan.trade.NftTokenTrade;

public interface NftSalesRetriever {

    public Stream<NftTokenTrade> retrieveNewSales(long lastBlock, String tokenContract);
    
    public String getRetrieverId();
    
}
