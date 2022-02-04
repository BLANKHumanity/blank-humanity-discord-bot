package de.zorro909.blank.wallet.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.wallet.DiscordWalletService;
import de.zorro909.blank.wallet.entities.DiscordWallet;
import de.zorro909.blank.wallet.entities.DiscordWalletSalt;
import de.zorro909.blank.wallet.persistence.DiscordWalletDao;
import de.zorro909.blank.wallet.persistence.DiscordWalletSaltDao;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscordWalletServiceImpl implements DiscordWalletService {

    @Autowired
    private DiscordWalletDao discordWalletDao;
    @Autowired
    private DiscordWalletSaltDao discordWalletSaltDao;

    private String messageFormat = "Hi, to connect your Ethereum Address to this Discord Account {0} on the Blank Humanity Server, please sign this random message: {1}";

    @Override
    @Transactional
    public String createWalletSalt(BlankUser user) {
	String salt = UUID.randomUUID().toString();

	DiscordWalletSalt walletSalt = discordWalletSaltDao
		.findByUser(user)
		.orElseGet(DiscordWalletSalt::new);

	walletSalt.setUser(user);
	walletSalt.setSalt(salt);

	discordWalletSaltDao.save(walletSalt);

	return salt;
    }

    @Override
    public Optional<DiscordWallet> registerWallet(SignatureData sigData,
	    String salt) {
	Optional<DiscordWalletSalt> walletSalt = discordWalletSaltDao
		.findBySalt(salt);

	if (walletSalt.isEmpty()) {
	    return Optional.empty();
	}

	DiscordWalletSalt wallet = walletSalt.get();
	String message = MessageFormatter
		.format(messageFormat, wallet.getUser().getDiscordId(),
			wallet.getSalt())
		.getMessage();

	BigInteger pubKey;
	try {
	    pubKey = Sign
		    .signedPrefixedMessageToKey(message.getBytes("UTF-8"),
			    sigData);
	} catch (SignatureException | UnsupportedEncodingException e) {
	    log.error("Invalid Signature for Wallet Registration!", e);
	    return Optional.empty();
	}

	DiscordWallet discordWallet = discordWalletDao
		.findByUser(wallet.getUser())
		.orElseGet(DiscordWallet::new);

	discordWallet.setUser(wallet.getUser());
	discordWallet.setWalletAddress(Numeric.toHexStringWithPrefix(pubKey));

	return Optional.of(discordWalletDao.save(discordWallet));
    }

    @Override
    public Optional<DiscordWallet> getWallet(BlankUser user) {
	return discordWalletDao.findByUser(user);
    }

}
