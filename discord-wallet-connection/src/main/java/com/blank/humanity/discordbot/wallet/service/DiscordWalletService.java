package com.blank.humanity.discordbot.wallet.service;

import java.util.List;
import java.util.Optional;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;

public interface DiscordWalletService {

    public String createVerifyWalletSalt(BlankUser user);

    public Optional<DiscordVerifiedWallet> registerVerifiedWallet(String sigData,
	    String salt);

    public List<DiscordVerifiedWallet> getWallets(BlankUser user);

    public Optional<BlankUser> findUserByWallet(String address);

}
