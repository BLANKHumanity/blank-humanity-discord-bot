package com.blank.humanity.discordbot.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.wallet.entities.DiscordWalletSalt;

@Repository
public interface DiscordVerifiedWalletSaltDao extends JpaRepository<DiscordWalletSalt, Integer>{

    public Optional<DiscordWalletSalt> findByUser(BlankUser user);
    
    public Optional<DiscordWalletSalt> findBySalt(String salt);
    
}
