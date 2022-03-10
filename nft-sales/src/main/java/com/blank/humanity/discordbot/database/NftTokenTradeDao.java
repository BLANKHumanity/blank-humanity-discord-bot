package com.blank.humanity.discordbot.database;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.etherscan.trade.NftTokenTrade;

@Repository
public interface NftTokenTradeDao extends JpaRepository<NftTokenTrade, Long> {

    @Query(value = "SELECT COALESCE(n.block,0) FROM nft_token_trade n WHERE nft_contract= :nftContract AND retriever_id= :retrieverId ORDER BY block DESC LIMIT 1", nativeQuery = true)
    public Optional<Long> findLastKnownBlock(String nftContract,
        String retrieverId);

}
