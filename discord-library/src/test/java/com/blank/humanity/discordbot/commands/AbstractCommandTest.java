package com.blank.humanity.discordbot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import java.util.List;
import java.util.function.Consumer;
import javax.validation.Validator;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.blank.humanity.discordbot.config.DiscordBotConfig;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.MessagesConfig;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.TransactionExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class AbstractCommandTest {

    @Mock
    Validator validator;

    @Mock
    private JDA jda;

    @Mock
    private BlankUserService blankUserService;

    @Mock
    private MessagesConfig messagesConfig;

    @Mock
    private CommandConfig commandConfig;

    @Mock
    private DiscordBotConfig discordBotConfig;

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private Environment environment;

    @Mock
    private TaskScheduler taskScheduler;

    private final String COMMAND_NAME = "testcommand";

    private final Long GUILD_ID = 123456L;

    private AbstractCommand abstractCommand;

    @BeforeEach
    public void test() {
        abstractCommand = Mockito
            .mock(AbstractCommand.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));
        ReflectionTestUtils.setField(abstractCommand, "validator", validator);
        ReflectionTestUtils.setField(abstractCommand, "jda", jda);
        ReflectionTestUtils
            .setField(abstractCommand, "blankUserService",
                blankUserService);
        ReflectionTestUtils
            .setField(abstractCommand, "messagesConfig", messagesConfig);
        ReflectionTestUtils
            .setField(abstractCommand, "commandConfig", commandConfig);
        ReflectionTestUtils
            .setField(abstractCommand, "discordBotConfig",
                discordBotConfig);
        ReflectionTestUtils
            .setField(abstractCommand, "transactionExecutor",
                transactionExecutor);
        ReflectionTestUtils
            .setField(abstractCommand, "environment", environment);
        ReflectionTestUtils
            .setField(abstractCommand, "taskScheduler", taskScheduler);

        Mockito
            .lenient()
            .doReturn(COMMAND_NAME)
            .when(abstractCommand)
            .getCommandName();
    }

    @Test
    void testUpdateCommandDefinition() {
        CommandDefinition commandDefinition = new CommandDefinition(
            "TestDescription", null, false, false, null);

        CommandData testCommandData = new CommandData(COMMAND_NAME,
            commandDefinition.getDescription());

        Guild testGuild = Mockito.mock(Guild.class);

        when(commandConfig.getCommandDefinition(COMMAND_NAME))
            .thenReturn(commandDefinition);
        doReturn(testCommandData)
            .when(abstractCommand)
            .createCommandData(Mockito.any());

        when(commandConfig.getGuildId()).thenReturn(GUILD_ID);
        when(jda.getGuildById(GUILD_ID)).thenReturn(testGuild);

        CommandCreateAction createAction = Mockito
            .mock(CommandCreateAction.class);

        when(testGuild.upsertCommand(testCommandData)).thenReturn(createAction);

        abstractCommand.updateCommandDefinition();

        verify(testGuild).upsertCommand(testCommandData);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testUpdateCommandDefinitionRestricted() {
        CommandDefinition commandDefinition = new CommandDefinition(
            "TestDescription", null, true, false,
            Lists.list(12345678l, 87654321l));

        CommandData testCommandData = new CommandData(COMMAND_NAME,
            commandDefinition.getDescription());

        Guild testGuild = Mockito.mock(Guild.class);

        long commandID = 54321L;

        when(commandConfig.getCommandDefinition(COMMAND_NAME))
            .thenReturn(commandDefinition);
        doReturn(testCommandData)
            .when(abstractCommand)
            .createCommandData(Mockito.any());

        when(commandConfig.getGuildId()).thenReturn(GUILD_ID);
        when(jda.getGuildById(GUILD_ID)).thenReturn(testGuild);

        CommandCreateAction createAction = Mockito
            .mock(CommandCreateAction.class);
        Command command = Mockito.mock(Command.class);

        Mockito.when(command.getIdLong()).thenReturn(commandID);

        mockRestActionQueue(createAction, command);

        when(testGuild.upsertCommand(testCommandData)).thenReturn(createAction);

        ArgumentCaptor<List<CommandPrivilege>> captor = ArgumentCaptor
            .forClass(List.class);

        RestAction<List<CommandPrivilege>> updatePrivilegesAction = Mockito
            .mock(RestAction.class);

        when(testGuild
            .updateCommandPrivilegesById(Mockito.eq(commandID),
                captor.capture())).thenReturn(updatePrivilegesAction);

        abstractCommand.updateCommandDefinition();

        List<CommandPrivilege> privilegeList = captor.getValue();
        assertThat(privilegeList)
            .isNotNull()
            .hasSize(2)
            .anyMatch(privilege -> privilege.getIdLong() == 12345678l)
            .anyMatch(privilege -> privilege.getIdLong() == 87654321l);
    }

    @SuppressWarnings("unchecked")
    protected <T> void mockRestActionQueue(RestAction<T> action, T result) {
        Mockito.doAnswer(invocation -> {
            invocation.getArgument(0, Consumer.class).accept(result);
            return null;
        }).when(action).queue(Mockito.any());
    }

}
