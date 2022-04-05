package com.blank.humanity.discordbot.wallet.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;

@Repository
public interface DiscordVerifiedWalletDao
    extends JpaRepository<DiscordVerifiedWallet, Integer> {

    public Optional<DiscordVerifiedWallet> findByWalletAddress(
        String walletAddress);

    public List<DiscordVerifiedWallet> findAllByUser(BlankUser user);

    public Optional<DiscordVerifiedWallet> findBySalt(String salt);

}
