package com.blank.humanity.discordbot.services.etherscan;

import java.util.stream.Stream;

import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTrade;

public interface NftSalesRetriever {

    public Stream<NftTokenTrade> retrieveNewSales(long lastBlock, String tokenContract);
    
    public String getRetrieverId();
    
}
