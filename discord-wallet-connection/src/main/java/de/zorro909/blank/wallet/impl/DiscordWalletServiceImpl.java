package de.zorro909.blank.wallet.impl;

import java.awt.font.NumericShaper;
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
    @Transactional
    public Optional<DiscordWallet> registerWallet(String sigData, String salt) {
	Optional<DiscordWalletSalt> walletSalt = discordWalletSaltDao
		.findBySalt(salt);

	if (walletSalt.isEmpty()) {
	    return Optional.empty();
	}

	DiscordWalletSalt saltWallet = walletSalt.get();
	String message = MessageFormatter
		.format(messageFormat, saltWallet.getUser().getDiscordId(),
			saltWallet.getSalt())
		.getMessage();

	byte[] signature = Numeric.hexStringToByteArray(sigData);
	byte[] messageData = message.getBytes();

	byte[] r = new byte[32];
	System.arraycopy(signature, 0, r, 0, 32);

	byte[] s = new byte[32];
	System.arraycopy(signature, 32, s, 0, 32);

	SignatureData signatureData = new SignatureData(signature[64], r, s);

	BigInteger pubKey;
	try {
	    pubKey = Sign
		    .signedPrefixedMessageToKey(messageData, signatureData);
	} catch (SignatureException e) {
	    log.error("Invalid Signature for Wallet Registration!", e);
	    return Optional.empty();
	}

	DiscordWallet discordWallet = discordWalletDao
		.findByUser(saltWallet.getUser())
		.orElseGet(DiscordWallet::new);

	discordWallet.setUser(saltWallet.getUser());
	discordWallet.setWalletAddress(Numeric.toHexStringWithPrefix(pubKey));

	Optional<DiscordWallet> optDiscordWallet = Optional
		.of(discordWalletDao.save(discordWallet));
	discordWalletSaltDao.delete(saltWallet);
	return optDiscordWallet;
    }

    @Override
    public Optional<DiscordWallet> getWallet(BlankUser user) {
	return discordWalletDao.findByUser(user);
    }

}
