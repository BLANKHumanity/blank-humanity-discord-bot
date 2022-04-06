package com.blank.humanity.discordbot.wallet.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.blank.humanity.discordbot.wallet.entities.NftOwnerEntity;

public interface NftOwnerEntityDao extends JpaRepository<NftOwnerEntity, Long> {

    @Query(value = "SELECT COALESCE(n.transfer_block,0) FROM nft_owner_entity n WHERE nft_contract= :nftContract ORDER BY transfer_block DESC LIMIT 1", nativeQuery = true)
    public Optional<Long> findLastKnownTransferBlock(String nftContract);

    public List<NftOwnerEntity> findByNftContractAndOwnerIn(String nftContract,
        List<String> addresses);

    public Optional<NftOwnerEntity> findByNftContractAndNftTokenId(
        String nftContract, long nftTokenId);

    @Query(value = "SELECT DISTINCT nft_contract FROM nft_owner_entity", nativeQuery = true)
    public List<String> findNftContracts();
}
