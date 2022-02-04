package de.zorro909.blank.wallet;

import java.util.Optional;

import org.web3j.crypto.Sign.SignatureData;

import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.wallet.entities.DiscordWallet;

public interface DiscordWalletService {

    public String createWalletSalt(BlankUser user);

    public Optional<DiscordWallet> registerWallet(SignatureData sigData,
	    String salt);

    public Optional<DiscordWallet> getWallet(BlankUser user);

}
