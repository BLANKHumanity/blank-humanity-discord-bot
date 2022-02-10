package com.blank.humanity.discordbot.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.database.GameMetadataDao;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.game.GameType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

@Component
public class GameServiceImpl implements GameService {

    @Autowired
    public GameMetadataDao gameMetadataDao;

    @Override
    @Transactional
    public GameMetadata saveGameMetadata(GameMetadata metadata) {
	return gameMetadataDao.save(metadata);
    }

    @Override
    public Optional<GameMetadata> getGameMetadata(BlankUser user,
	    GameType gameType) {
	return gameMetadataDao.findByUserAndGame(user, gameType);
    }

    @Override
    public Optional<GameMetadata> getGameMetadataById(long gameId) {
	return gameMetadataDao.findById(gameId);
    }

}
