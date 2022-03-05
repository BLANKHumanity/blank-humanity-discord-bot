package com.blank.humanity.discordbot.wallet.entities;

import java.util.NoSuchElementException;

import org.web3j.abi.datatypes.Address;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.BlankUserMetadata;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.wallet.rest.dto.AirdropWalletDto;

public class AirdropWallet {

    private BlankUserMetadata metadataObject;

    public AirdropWallet(BlankUserMetadata metadataObject) {
        if (metadataObject.getValue().isEmpty()) {
            throw new NoSuchElementException("Empty Wallet Value");
        }
        this.metadataObject = metadataObject;
    }

    public BlankUser getUser() {
        return metadataObject.getUser();
    }

    public Address getWallet() {
        return metadataObject.map(Address::new).orElse(Address.DEFAULT);
    }

    public AirdropWalletDto toDto(BlankUserService userService) {
        return AirdropWalletDto
            .builder()
            .discordId(getUser().getDiscordId())
            .wallet(metadataObject.getValue().orElse(null))
            .user(userService.getUsername(getUser()))
            .build();
    }

}
