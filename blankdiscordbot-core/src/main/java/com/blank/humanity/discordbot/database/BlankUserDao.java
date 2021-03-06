package com.blank.humanity.discordbot.database;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.BlankUser;

@Repository
public interface BlankUserDao extends JpaRepository<BlankUser, Long> {

    public Optional<BlankUser> findByDiscordId(Long discordId);
    
}
