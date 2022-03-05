package com.blank.humanity.discordbot.wallet.service;

import java.util.Optional;
import java.util.stream.Stream;

import org.web3j.abi.datatypes.Address;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.AirdropWallet;

import reactor.core.publisher.Flux;

public interface AirdropWalletService {

    public AirdropWallet setAirdropWallet(BlankUser user, Address wallet);
    
    public Optional<AirdropWallet> getAirdropWallet(BlankUser user);
    
    public Flux<AirdropWallet> listAllAirdropWallets();
    
    public Flux<AirdropWallet> listAirdropWalletsByRole(Long role);
    
}
