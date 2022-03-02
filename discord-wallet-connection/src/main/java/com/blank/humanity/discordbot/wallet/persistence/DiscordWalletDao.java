package com.blank.humanity.discordbot.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordWallet;

import lombok.NonNull;

@Repository
public interface DiscordWalletDao
	extends JpaRepository<DiscordWallet, Integer> {

    public Optional<DiscordWallet> findByUser(@NonNull BlankUser user);

}
