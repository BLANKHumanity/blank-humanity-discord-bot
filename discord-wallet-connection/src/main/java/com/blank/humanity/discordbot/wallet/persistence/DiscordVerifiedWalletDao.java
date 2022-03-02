package com.blank.humanity.discordbot.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;

import lombok.NonNull;

@Repository
public interface DiscordVerifiedWalletDao
	extends JpaRepository<DiscordVerifiedWallet, Integer> {

    public Optional<DiscordVerifiedWallet> findByUser(@NonNull BlankUser user);

}
