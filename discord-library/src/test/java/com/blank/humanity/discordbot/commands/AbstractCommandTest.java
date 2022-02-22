package com.blank.humanity.discordbot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collections;
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
import org.mockito.stubbing.Answer;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;

import com.blank.humanity.discordbot.config.DiscordBotConfig;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.MessagesConfig;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.TransactionExecutor;
import com.blank.humanity.discordbot.utils.menu.ReactionMenu;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

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
        CommandDefinition commandDefinition = mockCommandDefinition(
            COMMAND_NAME, "TestDescription", false, false);

        CommandData testCommandData = Commands.slash(COMMAND_NAME,
            commandDefinition.getDescription());

        Guild testGuild = Mockito.mock(Guild.class);

        when(commandConfig.getCommandDefinition(COMMAND_NAME))
            .thenReturn(commandDefinition);
        doReturn(testCommandData)
            .when(abstractCommand)
            .createCommandData(Mockito.notNull());

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
        CommandDefinition commandDefinition = mockCommandDefinition(
            COMMAND_NAME, "TestDescription", false, true, 12345678l, 87654321l);

        CommandData testCommandData = Commands.slash(COMMAND_NAME,
            commandDefinition.getDescription());

        Guild testGuild = Mockito.mock(Guild.class);

        long commandID = 54321L;

        doReturn(testCommandData)
            .when(abstractCommand)
            .createCommandData(Mockito.notNull());

        when(commandConfig.getGuildId()).thenReturn(GUILD_ID);
        when(jda.getGuildById(GUILD_ID)).thenReturn(testGuild);

        Command command = Mockito.mock(Command.class);

        Mockito.when(command.getIdLong()).thenReturn(commandID);

        CommandCreateAction createAction = mockRestActionQueue(
            CommandCreateAction.class, command);

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

    @Test
    void testSetupCommand() throws InterruptedException {
        doNothing().when(abstractCommand).updateCommandDefinition();

        abstractCommand.setupCommand();

        verify(jda).awaitReady();
        verify(abstractCommand).updateCommandDefinition();
        verify(jda).addEventListener(abstractCommand);
    }

    @SuppressWarnings("unchecked")
    private void testOnSlashCommandInteraction(SlashCommandInteractionEvent event,
        String[] embedReplies,
        Exception callExceptionHandler) {
        mockCommandDefinition(COMMAND_NAME, "TestDescription", false, false);
        mockCommandData(COMMAND_NAME, "TestDescription");

        when(event.getName()).thenReturn(COMMAND_NAME);

        TextChannel mockChannel = mockTextChannel(1234l);

        when(event.getChannel()).thenReturn(mockChannel);

        when(commandConfig.getHiddenCommandChannels())
            .thenReturn(Collections.emptyList());

        ReplyCallbackAction action = mock(ReplyCallbackAction.class);

        when(event.deferReply(false)).thenReturn(action);

        doAnswer(invocation -> {
            invocation
                .getArgument(0, TransactionCallback.class)
                .doInTransaction(null);
            if (callExceptionHandler != null) {
                invocation
                    .getArgument(1, Consumer.class)
                    .accept(callExceptionHandler);
            }
            invocation.getArgument(2, Consumer.class).accept(null);
            return null;
        })
            .when(transactionExecutor)
            .executeAsTransaction(Mockito.notNull(), Mockito.notNull(),
                Mockito.notNull());

        InteractionHook hook = mock(InteractionHook.class);

        when(event.getHook()).thenReturn(hook);

        WebhookMessageUpdateAction<Message> restAction = mock(
            WebhookMessageUpdateAction.class);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor
            .forClass(MessageEmbed.class);

        when(hook.editOriginalEmbeds(embedCaptor.capture()))
            .thenReturn(restAction);

        abstractCommand.onSlashCommandInteraction(event);

        verify(transactionExecutor, atLeast(1))
            .executeAsTransaction(Mockito.notNull(), Mockito.notNull(),
                Mockito.notNull());
        verify(transactionExecutor, atMost(2))
            .executeAsTransaction(Mockito.notNull(), Mockito.notNull(),
                Mockito.notNull());

        verify(abstractCommand).onCommand(event);

        List<MessageEmbed> embeds = embedCaptor.getAllValues();

        assertThat(embeds).isNotNull().hasSize(embedReplies.length);
        for (int i = 0; i < embedReplies.length; i++) {
            String expectedReply = embedReplies[i];
            assertThat(embeds.get(i).getDescription()).contains(expectedReply);
        }
    }

    @Test
    void testTransactionExceptionHandler() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);

        Exception exc = new Exception("Test Exception");

        mockMessageType("ERROR_MESSAGE", "Test ERROR_MESSAGE: %(errorMessage)");

        testOnSlashCommandInteraction(event,
            new String[] {
                "Test ERROR_MESSAGE: This command threw this error 'Test Exception'" },
            exc);
    }

    @Test
    void testTransactionFinishHandler() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);

        mockOnCommand(event, "Working");

        testOnSlashCommandInteraction(event, new String[] { "Working" }, null);
    }

    @Test
    void testMissingReply() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);

        mockMessageType("ERROR_MESSAGE", "Test ERROR_MESSAGE: %(errorMessage)");

        testOnSlashCommandInteraction(event,
            new String[] {
                "Test ERROR_MESSAGE: This command somehow didn't respond!" },
            null);
    }

    @Test
    void testAddReactionMenu() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);

        mockOnCommand(event, "Working");

        ReactionMenu menu = mock(ReactionMenu.class);

        abstractCommand.addReactionMenu(event, menu);

        testOnSlashCommandInteraction(event, new String[] { "Working" }, null);

        verify(menu)
            .buildMenu(Mockito.eq(jda), Mockito.any(),
                Mockito.eq(taskScheduler), Mockito.eq(transactionExecutor));
    }

    @Test
    void testAddLongRunningTask() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class);

        mockOnCommand(event, "Working");

        Subtask task = mock(Subtask.class);

        abstractCommand.addLongRunningTask(event, task);

        testOnSlashCommandInteraction(event, new String[] { "Working" }, null);

        verify(task)
            .accept(Mockito.notNull());
    }

    private void mockOnCommand(SlashCommandInteraction event, String message) {
        Answer<?> setEmbed = invocation -> {
            MessageEmbed embed = mock(MessageEmbed.class);
            lenient()
                .when(embed.getDescription())
                .thenReturn(message);
            abstractCommand.reply(event, embed);
            return null;
        };

        doAnswer(setEmbed)
            .when(abstractCommand)
            .onCommand(Mockito.notNull());
    }

    private CommandDefinition mockCommandDefinition(String commandName,
        String description,
        boolean hidden, boolean roleRestricted, Long... roles) {
        CommandDefinition commandDefinition = new CommandDefinition(description,
            null, roleRestricted, hidden, Lists.list(roles));

        ReflectionTestUtils
            .setField(abstractCommand, "commandDefinition", commandDefinition);

        lenient()
            .when(commandConfig.getCommandDefinition(commandName))
            .thenReturn(commandDefinition);

        return commandDefinition;
    }

    private void mockMessageType(String messageType, String format) {
        when(environment.getProperty("messages." + messageType))
            .thenReturn(format);
    }

    private CommandData mockCommandData(String commandName,
        String description) {
        CommandData data = Commands.slash(commandName, description);

        ReflectionTestUtils.setField(abstractCommand, "commandData", data);

        return data;
    }

    private TextChannel mockTextChannel(long id) {
        return mockChannel(TextChannel.class, id);
    }

    private <T extends Channel> T mockChannel(Class<T> channelClass, long id) {
        T channel = mock(channelClass);

        when(channel.getIdLong()).thenReturn(id);

        return channel;
    }

    @SuppressWarnings("unchecked")
    protected <T extends RestAction<R>, R> T mockRestActionQueue(
        Class<T> actionClass, R result) {
        T action = mock(actionClass);

        Mockito.doAnswer(invocation -> {
            invocation.getArgument(0, Consumer.class).accept(result);
            return null;
        }).when(action).queue(Mockito.notNull());

        return action;
    }

}
