package com.blank.humanity.discordbot.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.zorro909.blank.BlankDiscordBot.entities.game.GameMetadata;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;

@Repository
public interface GameMetadataDao extends JpaRepository<GameMetadata, Long> {

    public Optional<GameMetadata> findByUserAndGame(BlankUser user,
	    GameType game);
    
    public List<GameMetadata> findAllByGame(GameType game);

}
