package com.blank.humanity.discordbot.commands.games;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.FileSendRequest;
import com.blank.humanity.discordbot.commands.games.messages.GenericGameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GenericGameMessageType;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.config.commands.games.GameConfig;
import com.blank.humanity.discordbot.config.commands.games.GameDefinition;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.exceptions.game.UnknownGameIdException;
import com.blank.humanity.discordbot.services.GameService;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;
import com.blank.humanity.discordbot.utils.menu.impl.ComponentMenuBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

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

    /**
     * Loads gameDefinition automatically from the {@linkplain GameConfig}
     */
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
            setMenu(menu);
        }
    }

    /**
     * Sends a message of type
     * {@linkplain GenericGameMessageType#GAME_ON_COOLDOWN} for the current
     * game.
     * 
     * @param metadata GameMetadata of currently executing game.
     */
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

    /**
     * Retrieves or creates GameMetadata for Game execution
     * 
     * @return New GameMetadata Entity if one doesn't exist for the current
     *         user.
     */
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

    /**
     * Checks if User is able to start playing a new Game based on the Game's
     * Cooldown.
     * 
     * @param metadata GameMetadata for current User
     * @return True if user is allowed to play.
     */
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

    /**
     * Creates a ComponentMenuBuilder that allows creating a modern Component
     * (Buttons, SelectMenu, etc.) Menu.
     * 
     * @return The configured ComponentMenuBuilder
     */
    protected ComponentMenuBuilder componentMenu() {
        ComponentMenuBuilder builder = componentMenuBuilderProvider.getObject();
        builder
            .addWrapper(GenericComponentInteractionCreateEvent.class,
                new GameInteractionEventExecutor<>(this));

        return builder;
    }

    /**
     * Is called when a new Game is started by a Player. Always caused by a
     * SlashCommand.<br>
     * Implementation can be done similarly to
     * {@linkplain AbstractCommand#onCommand(GenericCommandInteractionEvent)}.
     * 
     * @param event    {@linkplain GenericCommandInteractionEvent}
     * @param user     The Player '{@linkplain BlankUser}'
     * @param metadata GameMetadata for current game/user
     * @see AbstractCommand#onCommand
     * @return A DiscordMenu if one should be added to the reply. Can be null.
     */
    @Nullable
    protected abstract DiscordMenu onGameStart(
        GenericCommandInteractionEvent event,
        BlankUser user, GameMetadata metadata);

    /**
     * Is called when a running Game is continued by a Player. May be invoked by
     * a SlashCommand or other interactions like a menu.<br>
     * Most protected methods like
     * {@link #reply(com.blank.humanity.discordbot.utils.FormattingData...)}
     * etc. will work, however execution-specific getters like
     * {@linkplain #getCommandEvent()} are not guaranteed to work.
     * 
     * @param user     The Player '{@linkplain BlankUser}'
     * @param metadata GameMetadata for current game/user
     * @param argument Argument object for interactions. Usually a string, can
     *                 be a List for SlashCommands({@linkplain OptionMapping})
     *                 or SelectMenu interactions
     * @return
     */
    protected abstract DiscordMenu onGameContinue(BlankUser user,
        GameMetadata metadata, Object argument);

    /**
     * Aborts the execution of a game. LastPlayed is not set, so the player can
     * immediately retry.
     * 
     * @param metadata GameMetadata for current game/user
     */
    protected void abort(GameMetadata metadata) {
        metadata.clearMetadata();
        metadata.setGameFinished(true);
        gameService.saveGameMetadata(metadata);
    }

    /**
     * Finishes a game. Clears up remaining Metadata and sets LastPlayed to
     * current time.<br>
     * Important: Must not be used inside a long running task / menu timeoutTask!
     * 
     * @param metadata GameMetadata for current game/user
     */
    protected void finish(GameMetadata metadata) {
        metadata.clearMetadata();
        metadata.setGameFinished(true);
        metadata.setLastPlayed(LocalDateTime.now());
        gameService.saveGameMetadata(metadata);
    }

    /**
     * Finishes a game. Clears up remaining Metadata and sets LastPlayed to
     * current time.<br>
     * Uses gameId to fetch the GameMetadata on execution. Allowing finishing
     * games within long running tasks.
     * 
     * @param gameId GameMetadata for current game/user
     */
    protected void finish(long gameId) {
        Optional<GameMetadata> metadataGetter = gameService
            .getGameMetadataById(gameId);
        if (metadataGetter.isEmpty()) {
            throw new UnknownGameIdException(
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
    protected final List<FileSendRequest> getUnsentFiles(){
        return super.getUnsentFiles();
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
     * @return Win Amount
     */
    protected int calculateWinnings(long betAmount) {
        return (int) Math
            .round(getGameDefinition().getWinningsMultiplier() * betAmount);
    }

}
