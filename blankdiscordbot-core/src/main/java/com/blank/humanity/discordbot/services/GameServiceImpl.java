package com.blank.humanity.discordbot.services;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.config.commands.games.GameDefinition;
import com.blank.humanity.discordbot.database.GameMetadataDao;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;

@Component
public class GameServiceImpl implements GameService {

    @Autowired
    public GameMetadataDao gameMetadataDao;

    @PostConstruct
    @Transactional
    private void clearPendingGames() {
	gameMetadataDao
		.findAll()
		.stream()
		.filter(metadata -> !metadata.isGameFinished())
		.forEach(metadata -> metadata.setGameFinished(true));
    }

    @Override
    @Transactional
    public GameMetadata saveGameMetadata(GameMetadata metadata) {
	return gameMetadataDao.save(metadata);
    }

    @Override
    public Optional<GameMetadata> getGameMetadata(BlankUser user,
	    String gameName) {
	return gameMetadataDao.findByUserAndGame(user, gameName);
    }

    @Override
    public Optional<GameMetadata> getGameMetadataById(long gameId) {
	return gameMetadataDao.findById(gameId);
    }

}
