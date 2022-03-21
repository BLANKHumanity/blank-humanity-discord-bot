package com.blank.humanity.discordbot.wallet.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.web3j.abi.datatypes.Address;

import com.blank.humanity.discordbot.entities.user.BlankUser;

public interface NftResolverService {

    public CompletableFuture<String> findOwner(String nftContractAddress,
        long nftId);

    public CompletableFuture<Optional<BlankUser>> findBlankUserOwner(
        String nftContractAddress, long nftId);

    public List<Long> findOwnedNFTs(String nftContractAddress, Address owner);

    public List<Long> findOwnedNFTs(String nftContractAddress, BlankUser owner);

    public Optional<byte[]> fetchNftImage(String initializerAddress,
        long initializerId);
}
