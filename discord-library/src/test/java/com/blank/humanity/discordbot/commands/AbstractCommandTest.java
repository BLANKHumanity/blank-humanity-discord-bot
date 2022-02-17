package com.blank.humanity.discordbot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.blank.humanity.discordbot.utils.FormattingData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

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
        CommandDefinition commandDefinition = mockCommandDefinition(
            COMMAND_NAME, "TestDescription", false, true, 12345678l, 87654321l);

        CommandData testCommandData = new CommandData(COMMAND_NAME,
            commandDefinition.getDescription());

        Guild testGuild = Mockito.mock(Guild.class);

        long commandID = 54321L;

        doReturn(testCommandData)
            .when(abstractCommand)
            .createCommandData(Mockito.any());

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
    private void testOnSlashCommand(SlashCommandEvent event,
        String[] embedReplies,
        Exception callExceptionHandler) {
        mockCommandDefinition(COMMAND_NAME, "TestDescription", false, false);
        mockCommandData(COMMAND_NAME, "TestDescription");

        when(event.getName()).thenReturn(COMMAND_NAME);

        TextChannel mockChannel = mockTextChannel(1234l);

        when(event.getChannel()).thenReturn(mockChannel);

        when(commandConfig.getHiddenCommandChannels())
            .thenReturn(Collections.emptyList());

        ReplyAction action = mock(ReplyAction.class);

        when(event.deferReply(false)).thenReturn(action);

        doNothing().when(abstractCommand).onCommand(Mockito.any());

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
            .executeAsTransaction(Mockito.any(), Mockito.any(), Mockito.any());

        InteractionHook hook = mock(InteractionHook.class);

        when(event.getHook()).thenReturn(hook);

        WebhookMessageUpdateAction<Message> restAction = mock(
            WebhookMessageUpdateAction.class);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor
            .forClass(MessageEmbed.class);

        when(hook.editOriginalEmbeds(embedCaptor.capture()))
            .thenReturn(restAction);

        abstractCommand.onSlashCommand(event);

        verify(transactionExecutor)
            .executeAsTransaction(Mockito.any(), Mockito.any(), Mockito.any());
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
        SlashCommandEvent event = mock(SlashCommandEvent.class);

        Exception exc = new Exception("Test Exception");

        mockSendErrorMessage();

        testOnSlashCommand(event, new String[] { "Test Exception" }, exc);
    }

    private void mockSendErrorMessage() {
        Answer<?> setEmbed = invocation -> {
            Map<SlashCommandEvent, MessageEmbed[]> cachedEmbeds = new HashMap<>();
            MessageEmbed embed = mock(MessageEmbed.class);
            lenient()
                .when(embed.getDescription())
                .thenReturn(invocation.getArgument(1));
            cachedEmbeds
                .put(invocation.getArgument(0), new MessageEmbed[] { embed });
            ReflectionTestUtils
                .setField(AbstractCommand.class, "cachedEmbeds", cachedEmbeds);
            return null;
        };

        doAnswer(setEmbed)
            .when(abstractCommand)
            .sendErrorMessage(Mockito.any(), Mockito.any());
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

    private CommandData mockCommandData(String commandName,
        String description) {
        CommandData data = new CommandData(commandName, description);

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
        }).when(action).queue(Mockito.any());

        return action;
    }

}
