package com.blank.humanity.discordbot.services;

import java.util.Optional;

import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.game.GameType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

public interface GameService {

    public GameMetadata saveGameMetadata(GameMetadata metadata);
    
    public Optional<GameMetadata> getGameMetadata(BlankUser user, GameType gameType);
    
    public Optional<GameMetadata> getGameMetadataById(long gameId);
}
