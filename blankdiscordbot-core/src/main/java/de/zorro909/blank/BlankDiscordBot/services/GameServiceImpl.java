package de.zorro909.blank.BlankDiscordBot.services;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.database.GameMetadataDao;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameMetadata;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;

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
