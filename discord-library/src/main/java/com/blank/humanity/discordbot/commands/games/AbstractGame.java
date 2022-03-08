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
import com.blank.humanity.discordbot.config.commands.CommandConfig;
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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;

@Slf4j
@Getter
public abstract class AbstractGame extends AbstractCommand {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    private CommandConfig commandConfig;

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

        GameMetadata metadata = retrieveGameMetadata();

        if (!isAbleToPlay(metadata)) {
            sendCooldownResponse(metadata);
            return;
        }

        DiscordMenu menu = null;
        if (metadata.isGameFinished()) {
            log
                .info("Starting a new Game of {} for {} (gameId: {})",
                    getCommandName(), user.getId(), metadata.getId());
            // Previous Game was finished
            metadata.setGameFinished(false);
            menu = onGameStart(event, user, metadata);
        } else {
            log
                .info(
                    "Continuing a Game of {} via SlashCommand for {} (gameId: {})",
                    getCommandName(), user.getId(), metadata.getId());
            menu = onGameContinue(user, metadata, event.getOptions());
        }
        if (menu != null) {
            addMenu(menu);
        }
    }

    private void sendCooldownResponse(GameMetadata metadata) {
        long seconds = ChronoUnit.SECONDS
            .between(getEarliestPossibleTimeToHavePlayedLast(),
                metadata.getLastPlayed());

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        reply(getBlankUserService()
            .createFormattingData(getUser(),
                GenericGameMessageType.GAME_ON_COOLDOWN)
            .dataPairing(GenericGameFormatDataKey.COOLDOWN_MINUTES,
                minutes)
            .dataPairing(GenericGameFormatDataKey.COOLDOWN_SECONDS,
                remainingSeconds)
            .dataPairing(GenericGameFormatDataKey.GAME_NAME,
                getGameDefinition().getDisplayName())
            .build());
    }

    private GameMetadata retrieveGameMetadata() {
        Optional<GameMetadata> gameMetadata = gameService
            .getGameMetadata(getUser(), getCommandName());

        if (gameMetadata.isPresent()) {
            return gameMetadata.get();
        } else {
            GameMetadata metadata = new GameMetadata();
            metadata.setGame(getCommandName());
            metadata.setGameFinished(true);
            metadata.setLastPlayed(LocalDateTime.of(2000, 1, 1, 0, 0));
            metadata.setUser(getUser());
            return gameService.saveGameMetadata(metadata);
        }
    }

    private boolean isAbleToPlay(GameMetadata metadata) {
        return metadata
            .getLastPlayed()
            .isBefore(getEarliestPossibleTimeToHavePlayedLast());
    }

    private LocalDateTime getEarliestPossibleTimeToHavePlayedLast() {
        return LocalDateTime
            .now()
            .minus(getGameDefinition().getCooldownAmount(),
                getGameDefinition().getCooldownTimeUnit());
    }

    protected ComponentMenuBuilder componentMenu() {
        ComponentMenuBuilder builder = componentMenuBuilderProvider.getObject();
        builder
            .addWrapper(GenericComponentInteractionCreateEvent.class,
                new GameInteractionEventExecutor<>(this));

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
        gameService.saveGameMetadata(metadata);
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
    }

    @Override
    protected final void setUser(BlankUser user) {
        super.setUser(user);
    }

    @Override
    protected final void setMember(Member member) {
        super.setMember(member);
    }

    @Override
    protected final MessageEmbed[] getUnsentReply() {
        return super.getUnsentReply();
    }

    @Override
    protected final void clearThreadLocals() {
        super.clearThreadLocals();
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
