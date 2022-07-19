package com.blank.humanity.discordbot.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blank.humanity.discordbot.wallet.entities.InitializerEmoteUnlock;

public interface InitializerEmoteUnlockDao
    extends JpaRepository<InitializerEmoteUnlock, Integer> {

    public Optional<InitializerEmoteUnlock> findByInitializerAndEmote(
        int initializer, String emote);

    public boolean existsByInitializerAndEmote(int initializer, String emote);

}
