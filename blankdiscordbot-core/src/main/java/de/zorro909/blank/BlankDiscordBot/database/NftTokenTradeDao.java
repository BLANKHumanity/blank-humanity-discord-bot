package de.zorro909.blank.BlankDiscordBot.database;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.trade.NftTokenTrade;

@Repository
public interface NftTokenTradeDao extends JpaRepository<NftTokenTrade, Long> {

    @Query(value = "SELECT COALESCE(n.block,0) FROM nft_token_trade n WHERE nft_contract= :nftContract ORDER BY block DESC LIMIT 1", nativeQuery = true)
    public Optional<Long> findLastKnownBlock(String nftContract);

}
