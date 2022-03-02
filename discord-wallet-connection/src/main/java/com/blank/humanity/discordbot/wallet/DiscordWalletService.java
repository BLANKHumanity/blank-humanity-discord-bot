package com.blank.humanity.discordbot.wallet;

import java.util.Optional;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordWallet;

public interface DiscordWalletService {

    public String createWalletSalt(BlankUser user);

    public Optional<DiscordWallet> registerWallet(String sigData,
	    String salt);

    public Optional<DiscordWallet> getWallet(BlankUser user);

}
