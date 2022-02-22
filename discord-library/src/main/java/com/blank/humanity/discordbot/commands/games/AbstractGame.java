package com.blank.humanity.discordbot.commands.games;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.games.messages.GenericGameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GenericGameMessageType;
import com.blank.humanity.discordbot.config.commands.games.GameConfig;
import com.blank.humanity.discordbot.config.commands.games.GameDefinition;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.GameService;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.menu.ReactionMenu;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

@Getter
public abstract class AbstractGame extends AbstractCommand {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameConfig gameConfig;

    private GameDefinition gameDefinition;

    private MessageEmbed cachedEdit;

    @PostConstruct
    private void loadGameDefinition() {
        this.gameDefinition = gameConfig.getGames().get(getCommandName());
        Objects
            .requireNonNull(gameDefinition, "Game Definition for '"
                + getCommandName() + "' does not exist!");
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
        BlankUser user = getBlankUserService().getUser(event);

        Optional<GameMetadata> gameMetadata = gameService
            .getGameMetadata(user, getCommandName());

        GameMetadata metadata = new GameMetadata();

        if (gameMetadata.isPresent()) {
            metadata = gameMetadata.get();

            LocalDateTime possibleEarlierTime = LocalDateTime
                .now()
                .minus(getGameDefinition().getCooldownAmount(),
                    getGameDefinition().getCooldownTimeUnit());

            if (metadata.getLastPlayed().isAfter(possibleEarlierTime)) {
                long minutes = ChronoUnit.MINUTES
                    .between(possibleEarlierTime, metadata.getLastPlayed());
                long seconds = ChronoUnit.SECONDS
                    .between(possibleEarlierTime, metadata.getLastPlayed())
                    % 60;

                reply(event, getBlankUserService()
                    .createFormattingData(user,
                        GenericGameMessageType.GAME_ON_COOLDOWN)
                    .dataPairing(GenericGameFormatDataKey.COOLDOWN_MINUTES,
                        minutes)
                    .dataPairing(GenericGameFormatDataKey.COOLDOWN_SECONDS,
                        seconds)
                    .dataPairing(GenericGameFormatDataKey.GAME_NAME,
                        getGameDefinition().getDisplayName())
                    .build());
                return;
            }
        } else {
            metadata.setGame(getCommandName());
            metadata.setGameFinished(true);
            metadata.setLastPlayed(LocalDateTime.of(2000, 1, 1, 0, 0));
            metadata.setUser(user);
            metadata = gameService.saveGameMetadata(metadata);
        }

        ReactionMenu menu = null;
        if (metadata.isGameFinished()) {
            // Previous Game was finished
            metadata.setGameFinished(false);
            menu = onGameStart(event, user, metadata);
        } else {
            synchronized (this) {
                cachedEdit = null;
                menu = onGameContinue(user, metadata, event.getOptions(),
                    formattingData -> {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setDescription(format(formattingData));

                        cachedEdit = builder.build();
                    });
                reply(event, cachedEdit);
            }
        }
        if (menu != null) {
            addReactionMenu(event, menu);
        }
    }

    private boolean reactionInteractionWrapper(MessageReactionAddEvent event,
        ReactionMenu menu, Object argument) {
        BlankUser user = getBlankUserService()
            .getUser(event.retrieveMember().complete());

        Optional<GameMetadata> gameMetadata = gameService
            .getGameMetadata(user, getCommandName());

        if (gameMetadata.isEmpty()) {
            return false;
        }

        GameMetadata metadata = gameMetadata.get();

        ReactionMenu newMenu = null;

        synchronized (this) {
            cachedEdit = null;
            newMenu = onGameContinue(user, metadata, argument,
                formattingData -> {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setDescription(format(formattingData));

                    cachedEdit = builder.build();
                });
            if (cachedEdit != null) {
                event
                    .getChannel()
                    .editMessageEmbedsById(event.getMessageIdLong(),
                        cachedEdit)
                    .queue();
            }
        }
        if (newMenu != null || metadata.isGameFinished()) {
            menu.discard();
            if (newMenu != null) {
                newMenu
                    .buildMenu(getJda(),
                        event
                            .getChannel()
                            .retrieveMessageById(
                                event.getMessageIdLong())
                            .complete(),
                        getTaskScheduler(), getTransactionExecutor());
            }
        }

        return true;
    }

    protected void createMenuEntry(ReactionMenu menu, String emoji,
        Object argument) {
        menu
            .addMenuAction(emoji, event -> reactionInteractionWrapper(event,
                menu, argument));
    }

    protected abstract ReactionMenu onGameStart(SlashCommandInteraction event,
        BlankUser user, GameMetadata metadata);

    protected abstract ReactionMenu onGameContinue(BlankUser user,
        GameMetadata metadata, Object argument,
        Consumer<FormattingData> messageEdit);

    protected void abort(GameMetadata metadata) {
        metadata.clearMetadata();
        metadata.setGameFinished(true);
    }

    protected void finish(GameMetadata metadata) {
        metadata.clearMetadata();
        metadata.setGameFinished(true);
        metadata.setLastPlayed(LocalDateTime.now());
    }

    protected void finish(long gameId) {
        Optional<GameMetadata> metadataGetter = gameService
            .getGameMetadataById(gameId);
        if (metadataGetter.isEmpty()) {
            throw new RuntimeException(
                "No Game with ID '" + gameId + "' found!");
        }
        GameMetadata metadata = metadataGetter.get();
        finish(metadata);
        gameService.saveGameMetadata(metadata);
    }

    /**
     * Calculates Winnings based on Winnings Multiplier Config Attribute.
     * Expects that the betAmount was previously taken from the player.
     * 
     * @param betAmount
     * @return
     */
    protected int calculateWinnings(long betAmount) {
        return (int) Math
            .round(getGameDefinition().getWinningsMultiplier() * betAmount);
    }

}
