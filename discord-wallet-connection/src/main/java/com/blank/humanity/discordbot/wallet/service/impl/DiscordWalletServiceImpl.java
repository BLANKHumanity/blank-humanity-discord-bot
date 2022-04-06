package com.blank.humanity.discordbot.wallet.service.impl;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;
import com.blank.humanity.discordbot.wallet.entities.DiscordWalletSalt;
import com.blank.humanity.discordbot.wallet.persistence.DiscordVerifiedWalletDao;
import com.blank.humanity.discordbot.wallet.persistence.DiscordVerifiedWalletSaltDao;
import com.blank.humanity.discordbot.wallet.service.DiscordWalletService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiscordWalletServiceImpl implements DiscordWalletService {

    @Autowired
    private DiscordVerifiedWalletDao discordWalletDao;
    @Autowired
    private DiscordVerifiedWalletSaltDao discordWalletSaltDao;

    private String messageFormat = "Hi, to connect your Ethereum Address to this Discord Account {} on the Blank Humanity Server, please sign this random message: {}";

    @Override
    @Transactional
    public String createVerifyWalletSalt(BlankUser user) {
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
    public ResponseEntity<Void> registerVerifiedWallet(
        String requestedAddress, String sigData, String salt) {
        boolean alreadyVerified = discordWalletDao
            .findBySalt(salt)
            .map(DiscordVerifiedWallet::getWalletAddress)
            .filter(verifiedAddress -> verifiedAddress
                .equalsIgnoreCase(requestedAddress))
            .isPresent();

        if (alreadyVerified) {
            return ResponseEntity.ok().build();
        }

        Optional<DiscordWalletSalt> walletSalt = discordWalletSaltDao
            .findBySalt(salt);

        if (walletSalt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DiscordWalletSalt saltWallet = walletSalt.get();
        String message = MessageFormatter
            .format(messageFormat, saltWallet.getUser().getDiscordId(),
                saltWallet.getSalt())
            .getMessage();

        byte[] signature = Numeric.hexStringToByteArray(sigData);
        byte[] messageData = message.getBytes();

        Optional<String> pubAddress = recoverAddressFromSignature(messageData,
            signature)
                .filter(address -> address.equalsIgnoreCase(requestedAddress));

        if (pubAddress.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<DiscordVerifiedWallet> existingRegistration = pubAddress
            .flatMap(discordWalletDao::findByWalletAddress);

        DiscordVerifiedWallet discordWallet = new DiscordVerifiedWallet();

        if (existingRegistration.isPresent()) {
            Long userId = existingRegistration.get().getUser().getId();
            if (userId.equals(saltWallet.getUser().getId())) {
                return ResponseEntity.ok().build();
            } else {
                discordWallet = existingRegistration.get();
            }
        }

        discordWallet.setSalt(salt);
        discordWallet.setSignature(sigData);
        discordWallet.setSignatureVersion(1);
        discordWallet.setUser(saltWallet.getUser());
        discordWallet.setWalletAddress(pubAddress.get());

        discordWalletDao.save(discordWallet);
        discordWalletSaltDao.delete(saltWallet);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public Optional<String> recoverAddressFromSignature(byte[] messageData,
        byte[] signature) {
        byte[] r = new byte[32];
        System.arraycopy(signature, 0, r, 0, 32);

        byte[] s = new byte[32];
        System.arraycopy(signature, 32, s, 0, 32);

        byte v = signature[64];
        if (v < 27) {
            v += 27;
        }

        SignatureData signatureData = new SignatureData(v, r, s);

        try {
            BigInteger pubKey = Sign
                .signedPrefixedMessageToKey(messageData, signatureData);
            return Optional.of("0x" + Keys.getAddress(pubKey));
        } catch (SignatureException e) {
            log.error("Invalid Signature for Wallet Registration!", e);
            return Optional.empty();
        }
    }

    @Override
    public List<DiscordVerifiedWallet> getWallets(BlankUser user) {
        return discordWalletDao.findAllByUser(user);
    }

    @Override
    public Optional<BlankUser> findUserByWallet(String address) {
        return discordWalletDao
            .findByWalletAddress(address)
            .map(DiscordVerifiedWallet::getUser);
    }

}
