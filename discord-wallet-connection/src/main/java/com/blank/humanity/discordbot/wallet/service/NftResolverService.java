package com.blank.humanity.discordbot.wallet.service;

import java.util.List;
import java.util.Optional;

import com.blank.humanity.discordbot.entities.user.BlankUser;

public interface NftResolverService {

    public Optional<String> findOwner(String nftContractAddress,
        long nftId);

    public Optional<BlankUser> findBlankUserOwner(
        String nftContractAddress, long nftId);

    public List<Long> findOwnedNFTs(String nftContractAddress, String... ownerAddresses);

    public List<Long> findOwnedNFTs(String nftContractAddress, BlankUser owner);

    public Optional<byte[]> fetchNftImage(String initializerAddress,
        long initializerId);
}
