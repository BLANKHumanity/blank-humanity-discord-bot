package com.blank.humanity.discordbot.commands.games;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Optional;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.blank.humanity.discordbot.config.commands.games.GameConfig;
import com.blank.humanity.discordbot.config.commands.games.GameDefinition;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.GameService;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class AbstractGameTest {

    @Mock
    private GameConfig gameConfig;

    @Mock
    private GameService gameService;

    @Mock
    private Logger log;

    private AbstractGame game;

    @BeforeEach
    void setup() {
        game = Mockito
            .mock(AbstractGame.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));
        ReflectionTestUtils
            .setField(game, "gameService", gameService, GameService.class);
        ReflectionTestUtils
            .setField(game, "gameConfig", gameConfig, GameConfig.class);
    }

    @Test
    void testLoadGameDefinition() {
        String commandName = "testCommand";
        setCommandName(commandName);

        GameDefinition gameDefinition = mock(GameDefinition.class);

        when(gameConfig.getGames())
            .thenReturn((HashMap<String, GameDefinition>) Maps
                .newHashMap(commandName, gameDefinition));

        assertThatNoException()
            .isThrownBy(() -> ReflectionTestUtils
                .invokeMethod(game, "loadGameDefinition"));
    }

    @Test
    void testLoadGameDefinitionException() {
        String commandName = "testCommand";
        setCommandName(commandName);

        when(gameConfig.getGames())
            .thenReturn(new HashMap<>());

        assertThatThrownBy(() -> ReflectionTestUtils
            .invokeMethod(game, "loadGameDefinition"))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void testOnCommand(@Mock BlankUser user, @Mock GameMetadata metadata,
        @Mock DiscordMenu menu, @Mock GenericCommandInteractionEvent event,
        @Mock ThreadLocal<DiscordMenu> localMenu) {
        GameDefinition gameDefinition = new GameDefinition();
        gameDefinition.setCooldownAmount(1l);
        gameDefinition.setCooldownTimeUnit(ChronoUnit.HOURS);

        setUser(user);
        setGameDefinition(gameDefinition);
        ReflectionTestUtils.setField(game, "localMenu", localMenu);

        when(metadata.getLastPlayed()).thenReturn(LocalDateTime.MIN);
        when(metadata.isGameFinished()).thenReturn(true);

        doReturn(Optional.of(metadata))
            .when(gameService)
            .getGameMetadata(Mockito.any(), Mockito.any());
        doReturn(menu)
            .when(game)
            .onGameStart(Mockito.any(), Mockito.any(), Mockito.any());

        ReflectionTestUtils.invokeMethod(game, "onCommand", event);

        verify(game).onGameStart(event, user, metadata);
        verify(localMenu).set(menu);
    }

    @Test
    void retrieveGameMetadataNewTest() {
        String commandName = "commandtest";
        setCommandName(commandName);
        BlankUser user = mock(BlankUser.class);
        setUser(user);

        when(gameService.getGameMetadata(user, commandName))
            .thenReturn(Optional.empty());
        when(gameService.saveGameMetadata(Mockito.any()))
            .thenAnswer(i -> i.getArgument(0));

        GameMetadata metadata = ReflectionTestUtils
            .invokeMethod(game, "retrieveGameMetadata");

        verify(gameService).saveGameMetadata(Mockito.any());
        assertThat(metadata).isNotNull();
        assertThat(metadata.getUser()).isEqualTo(user);
        assertThat(metadata.getGame()).isEqualTo(commandName);
        assertThat(metadata.getId()).isNull();
    }

    @Test
    void retrieveGameMetadataExistingTest() {
        String commandName = "commandtest";
        setCommandName(commandName);
        BlankUser user = mock(BlankUser.class);
        setUser(user);

        GameMetadata existingMetadata = new GameMetadata();

        when(gameService.getGameMetadata(user, commandName))
            .thenReturn(Optional.of(existingMetadata));

        GameMetadata metadata = ReflectionTestUtils
            .invokeMethod(game, "retrieveGameMetadata");

        verify(gameService, never()).saveGameMetadata(Mockito.any());
        assertThat(metadata).isEqualTo(existingMetadata);
    }

    private void setCommandName(String commandName) {
        doReturn(commandName).when(game).getCommandName();
    }

    @SuppressWarnings("unchecked")
    private void setUser(BlankUser user) {
        ThreadLocal<BlankUser> tl = mock(ThreadLocal.class);
        doReturn(user).when(tl).get();
        ReflectionTestUtils.setField(game, "localUser", tl);
    }

    private void setGameDefinition(GameDefinition gameDefinition) {
        ReflectionTestUtils.setField(game, "gameDefinition", gameDefinition);
    }

}
