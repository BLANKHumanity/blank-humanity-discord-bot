package com.blank.humanity.discordbot.wallet;

import java.util.Optional;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;

public interface DiscordWalletService {

    public String createVerifyWalletSalt(BlankUser user);

    public Optional<DiscordVerifiedWallet> registerVerifiedWallet(String sigData,
	    String salt);

    public Optional<DiscordVerifiedWallet> getWallet(BlankUser user);

}
