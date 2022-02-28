package com.blank.humanity.discordbot.commands.games;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.games.messages.GenericGameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GenericGameMessageType;
import com.blank.humanity.discordbot.config.commands.games.GameConfig;
import com.blank.humanity.discordbot.config.commands.games.GameDefinition;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.GameService;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;
import com.blank.humanity.discordbot.utils.menu.impl.ComponentMenuBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;

@Slf4j
@Getter
public abstract class AbstractGame extends AbstractCommand {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    private ObjectProvider<ComponentMenuBuilder> componentMenuBuilderProvider;

    private GameDefinition gameDefinition;

    @PostConstruct
    private void loadGameDefinition() {
        this.gameDefinition = gameConfig.getGames().get(getCommandName());
        Objects
            .requireNonNull(gameDefinition, "Game Definition for '"
                + getCommandName() + "' does not exist!");
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        BlankUser user = getUser();

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

                reply(getBlankUserService()
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

        DiscordMenu menu = null;
        if (metadata.isGameFinished()) {
            log
                .info("Starting new Game: " + getCommandName() + " for "
                    + user.getId() + " (game: " + metadata.getId() + ")");
            // Previous Game was finished
            metadata.setGameFinished(false);
            menu = onGameStart(event, user, metadata);
        } else {
            log
                .info("Continuing Game via SlashCommand: " + getCommandName()
                    + " for " + user.getId() + " (game: " + metadata.getId()
                    + ")");
            menu = onGameContinue(user, metadata, event.getOptions());
        }
        if (menu != null) {
            addMenu(menu);
        }
    }

    private boolean interactionEventWrapper(MessageChannel channel,
        long messageId, Member member, DiscordMenu menu, Object argument) {
        BlankUser user = getBlankUserService()
            .getUser(member);

        Optional<GameMetadata> gameMetadata = gameService
            .getGameMetadata(user, getCommandName());

        if (gameMetadata.isEmpty()) {
            return false;
        }

        GameMetadata metadata = gameMetadata.get();

        DiscordMenu newMenu = null;

        try {
            setUser(user);
            setMember(member);

            log
                .info("Continuing Game via Menu: " + getCommandName() + " for "
                    + user.getId() + " (game: " + metadata.getId() + ")");

            newMenu = onGameContinue(user, metadata, argument);
            if (getUnsentReply() != null) {
                channel
                    .editMessageEmbedsById(messageId, getUnsentReply())
                    .complete();
            }

            if (newMenu != null || metadata.isGameFinished()) {
                menu.discard();
                if (newMenu != null) {
                    newMenu
                        .buildMenu(getJda(),
                            channel
                                .retrieveMessageById(
                                    messageId)
                                .complete(),
                            getMenuService());
                }
            }
        } finally {
            clearThreadLocals();
        }
        return true;
    }

    protected ComponentMenuBuilder componentMenu() {
        ComponentMenuBuilder builder = componentMenuBuilderProvider.getObject();
        builder
            .addWrapper(ButtonInteractionEvent.class,
                (event, menu, argument) -> interactionEventWrapper(
                    event.getChannel(), event.getMessageIdLong(),
                    event.getMember(), menu, argument));

        builder
            .addWrapper(SelectMenuInteractionEvent.class,
                (event, menu, argument) -> {
                    Object arg = argument;
                    if (event.getSelectMenu().getMaxValues() > 1) {
                        arg = Arrays.asList(argument.split(","));
                    }
                    return interactionEventWrapper(event.getChannel(),
                        event.getMessageIdLong(), event.getMember(),
                        menu, arg);
                });

        return builder;
    }

    protected abstract DiscordMenu onGameStart(
        GenericCommandInteractionEvent event,
        BlankUser user, GameMetadata metadata);

    protected abstract DiscordMenu onGameContinue(BlankUser user,
        GameMetadata metadata, Object argument);

    protected void abort(GameMetadata metadata) {
        metadata.clearMetadata();
        metadata.setGameFinished(true);
    }

    protected void finish(GameMetadata metadata) {
        metadata.clearMetadata();
        metadata.setGameFinished(true);
        metadata.setLastPlayed(LocalDateTime.now());
        gameService.saveGameMetadata(metadata);
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
