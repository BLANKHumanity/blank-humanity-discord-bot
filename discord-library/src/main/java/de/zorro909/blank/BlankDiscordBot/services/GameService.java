package de.zorro909.blank.BlankDiscordBot.services;

import java.util.Optional;

import de.zorro909.blank.BlankDiscordBot.entities.game.GameMetadata;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;

public interface GameService {

    public GameMetadata saveGameMetadata(GameMetadata metadata);
    
    public Optional<GameMetadata> getGameMetadata(BlankUser user, GameType gameType);
    
    public Optional<GameMetadata> getGameMetadataById(long gameId);
}
