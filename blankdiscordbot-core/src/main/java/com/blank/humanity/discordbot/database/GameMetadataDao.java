package com.blank.humanity.discordbot.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.game.GameType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

@Repository
public interface GameMetadataDao extends JpaRepository<GameMetadata, Long> {

    public Optional<GameMetadata> findByUserAndGame(BlankUser user,
	    GameType game);
    
    public List<GameMetadata> findAllByGame(GameType game);

}
