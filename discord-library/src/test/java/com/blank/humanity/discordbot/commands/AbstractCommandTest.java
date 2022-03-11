package com.blank.humanity.discordbot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.exceptions.command.OutsideOfCommandContextException;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.CommandService;
import com.blank.humanity.discordbot.services.MenuService;
import com.blank.humanity.discordbot.services.MessageService;
import com.blank.humanity.discordbot.services.TransactionExecutor;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class AbstractCommandTest {

    @Mock
    private JDA jda;

    @Mock
    private BlankUserService blankUserService;

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private MenuService menuService;

    @Mock
    private MessageService messageService;

    @Mock
    private CommandService commandService;

    @Mock
    private ThreadLocal<GenericCommandInteractionEvent> commandEvent;

    @Mock
    private ThreadLocal<CommandAutoCompleteInteractionEvent> autoCompleteEvent;

    @Mock
    private ThreadLocal<BlankUser> localUser;

    @Mock
    private ThreadLocal<Member> localMember;

    @Mock
    private ThreadLocal<MessageEmbed[]> localEmbedsToSend;

    @Mock
    private ThreadLocal<DiscordMenu> localMenu;

    @Mock
    private ThreadLocal<Runnable> localCachedTasks;

    private AbstractCommand abstractCommand;

    @BeforeEach
    void setupThreadLocalMocks() {
        ReflectionTestUtils
            .setField(AbstractCommand.class, "commandEvent", commandEvent);
        ReflectionTestUtils
            .setField(AbstractCommand.class, "autoCompleteEvent",
                autoCompleteEvent);
        ReflectionTestUtils
            .setField(AbstractCommand.class, "localUser", localUser);
        ReflectionTestUtils
            .setField(AbstractCommand.class, "localMember", localMember);
        ReflectionTestUtils
            .setField(AbstractCommand.class, "localEmbedsToSend",
                localEmbedsToSend);
        ReflectionTestUtils
            .setField(AbstractCommand.class, "localMenu", localMenu);
        ReflectionTestUtils
            .setField(AbstractCommand.class, "localCachedTasks",
                localCachedTasks);
    }

    @BeforeEach
    void setupVariables() {
        abstractCommand = Mockito
            .mock(AbstractCommand.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));
        ReflectionTestUtils.setField(abstractCommand, "jda", jda);
        ReflectionTestUtils
            .setField(abstractCommand, "blankUserService",
                blankUserService);
        ReflectionTestUtils
            .setField(abstractCommand, "transactionExecutor",
                transactionExecutor);
        ReflectionTestUtils
            .setField(abstractCommand, "menuService", menuService);
        ReflectionTestUtils
            .setField(abstractCommand, "messageService", messageService);
        ReflectionTestUtils
            .setField(abstractCommand, "commandService", commandService);
        
    }

    @Test
    void testClearLocalThreads() {
        abstractCommand.clearThreadLocals();

        verify(commandEvent).remove();
        verify(autoCompleteEvent).remove();
        verify(localUser).remove();
        verify(localMember).remove();
        verify(localEmbedsToSend).remove();
        verify(localMenu).remove();
        verify(localCachedTasks).remove();
    }

    @Test
    void testSetupCommand() throws InterruptedException {
        this.abstractCommand.setupCommand();
        verify(commandService).registerCommand(abstractCommand);
    }

    @Test
    void testReceiveCommandInteractionMinimal() {

        GenericCommandInteractionEvent event = mock(
            GenericCommandInteractionEvent.class);

        InteractionHook hook = mock(InteractionHook.class);

        MessageEmbed embed = mock(MessageEmbed.class);
        MessageEmbed[] embeds = new MessageEmbed[] { embed };

        @SuppressWarnings("unchecked")
        WebhookMessageUpdateAction<Message> updateAction = mock(
            WebhookMessageUpdateAction.class);

        when(event.getHook()).thenReturn(hook);
        when(hook.editOriginalEmbeds(embeds)).thenReturn(updateAction);

        doNothing().when(abstractCommand).onCommand(event);
        doNothing().when(abstractCommand).clearThreadLocals();

        doReturn(event).when(commandEvent).get();
        doReturn(embeds).when(localEmbedsToSend).get();

        Boolean result = abstractCommand.receiveCommandInteraction(event);

        assertThat(result).isTrue();

        verify(abstractCommand).onCommand(event);
        verify(abstractCommand).clearThreadLocals();
        verify(updateAction).complete();

        verify(abstractCommand, never())
            .reply(Mockito.any(FormattingData[].class));
        verify(abstractCommand, never())
            .reply(Mockito.any(MessageEmbed[].class));
        verify(abstractCommand, never()).sendErrorMessage(Mockito.anyString());
    }

    @Test
    void testReceiveCommandInteractionAll() {

        GenericCommandInteractionEvent event = mock(
            GenericCommandInteractionEvent.class);

        InteractionHook hook = mock(InteractionHook.class);

        MessageEmbed embed = mock(MessageEmbed.class);
        MessageEmbed[] embeds = new MessageEmbed[] { embed };

        @SuppressWarnings("unchecked")
        WebhookMessageUpdateAction<Message> updateAction = mock(
            WebhookMessageUpdateAction.class);

        DiscordMenu menu = mock(DiscordMenu.class);
        Runnable task = mock(Runnable.class);

        Message message = mock(Message.class);

        when(event.getHook()).thenReturn(hook);
        when(hook.editOriginalEmbeds(embeds)).thenReturn(updateAction);
        when(updateAction.complete()).thenReturn(message);

        doNothing().when(abstractCommand).onCommand(event);
        doNothing().when(abstractCommand).clearThreadLocals();

        doReturn(event).when(commandEvent).get();
        doReturn(embeds).when(localEmbedsToSend).get();
        doReturn(menu).when(localMenu).get();
        doReturn(task).when(localCachedTasks).get();

        Boolean result = abstractCommand.receiveCommandInteraction(event);

        assertThat(result).isTrue();

        verify(abstractCommand).onCommand(event);
        verify(abstractCommand).clearThreadLocals();
        verify(updateAction).complete();
        verify(menu).buildMenu(jda, message, menuService);

        verify(abstractCommand, never())
            .reply(Mockito.any(FormattingData[].class));
        verify(abstractCommand, never())
            .reply(Mockito.any(MessageEmbed[].class));
    }

    @Test
    void testReceiveCommandInteractionMissingReply() {

        GenericCommandInteractionEvent event = mock(
            GenericCommandInteractionEvent.class);

        InteractionHook hook = mock(InteractionHook.class);

        @SuppressWarnings("unchecked")
        WebhookMessageUpdateAction<Message> updateAction = mock(
            WebhookMessageUpdateAction.class);

        when(event.getHook()).thenReturn(hook);
        when(hook.editOriginalEmbeds(Mockito.nullable(MessageEmbed[].class)))
            .thenReturn(updateAction);

        doNothing().when(abstractCommand).onCommand(event);
        doNothing().when(abstractCommand).clearThreadLocals();
        doNothing().when(abstractCommand).sendErrorMessage(Mockito.anyString());

        doReturn(event).when(commandEvent).get();

        Boolean result = abstractCommand.receiveCommandInteraction(event);

        assertThat(result).isTrue();

        verify(abstractCommand).onCommand(event);
        verify(abstractCommand).clearThreadLocals();
        verify(updateAction).complete();
        verify(abstractCommand).sendErrorMessage(Mockito.anyString());

        verify(abstractCommand, never())
            .reply(Mockito.any(FormattingData[].class));
        verify(abstractCommand, never())
            .reply(Mockito.any(MessageEmbed[].class));
    }

    @Test
    void testReceiveCommandInteractionException() {

        GenericCommandInteractionEvent event = mock(
            GenericCommandInteractionEvent.class);

        InteractionHook hook = mock(InteractionHook.class);

        MessageEmbed embed = mock(MessageEmbed.class);
        MessageEmbed[] embeds = new MessageEmbed[] { embed };

        @SuppressWarnings("unchecked")
        WebhookMessageUpdateAction<Message> updateAction = mock(
            WebhookMessageUpdateAction.class);

        when(event.getHook()).thenReturn(hook);
        when(hook.editOriginalEmbeds(embeds)).thenReturn(updateAction);

        doThrow(RuntimeException.class).when(abstractCommand).onCommand(event);
        doNothing().when(abstractCommand).clearThreadLocals();
        doNothing().when(abstractCommand).sendErrorMessage(Mockito.anyString());

        doReturn(embeds).when(localEmbedsToSend).get();
        doReturn(event).when(commandEvent).get();

        Boolean result = abstractCommand.receiveCommandInteraction(event);

        assertThat(result).isFalse();

        verify(abstractCommand).onCommand(event);
        verify(abstractCommand).clearThreadLocals();
        verify(updateAction).complete();
        verify(abstractCommand).sendErrorMessage(Mockito.anyString());

        verify(abstractCommand, never())
            .reply(Mockito.any(FormattingData[].class));
        verify(abstractCommand, never())
            .reply(Mockito.any(MessageEmbed[].class));
    }

    @Test
    void testSendErrorMessage() {

        String errorMessage = "This is supposed to be a error message!";

        ArgumentCaptor<FormattingData> dataCaptor = ArgumentCaptor
            .forClass(FormattingData.class);

        doNothing().when(abstractCommand).reply(dataCaptor.capture());

        this.abstractCommand.sendErrorMessage(errorMessage);

        FormattingData formattingData = dataCaptor.getValue();
        assertThat(formattingData)
            .isNotNull()
            .matches(
                data -> data.messageType() == GenericMessageType.ERROR_MESSAGE)
            .matches(data -> data
                .get(GenericFormatDataKey.ERROR_MESSAGE)
                .equals(errorMessage));
    }

    @Test
    void testReplyFormattingData() {
        BlankUser user = mock(BlankUser.class);
        doReturn(user).when(localUser).get();

        String testMessage = "TestMessage";

        FormattingData formattingData = mock(FormattingData.class);
        when(messageService.format(formattingData)).thenReturn(testMessage);

        ArgumentCaptor<MessageEmbed[]> embedsCapture = ArgumentCaptor
            .forClass(MessageEmbed[].class);
        doNothing().when(localEmbedsToSend).set(embedsCapture.capture());

        this.abstractCommand.reply(formattingData);

        MessageEmbed[] embeds = embedsCapture.getValue();

        assertThat(embeds)
            .isNotNull()
            .hasSize(1)
            .anyMatch(embed -> embed.getDescription().equals(testMessage));
    }

    @Test
    void testReplyFormattingDataError() {
        assertThatCode(() -> this.abstractCommand.reply((FormattingData) null))
            .isInstanceOf(OutsideOfCommandContextException.class);
    }

    @Test
    void testReplyMessageEmbed() {
        BlankUser user = mock(BlankUser.class);
        doReturn(user).when(localUser).get();

        MessageEmbed[] mockEmbeds = new MessageEmbed[] {
            mock(MessageEmbed.class) };

        ArgumentCaptor<MessageEmbed[]> embedsCapture = ArgumentCaptor
            .forClass(MessageEmbed[].class);
        doNothing().when(localEmbedsToSend).set(embedsCapture.capture());

        this.abstractCommand.reply(mockEmbeds);

        MessageEmbed[] embeds = embedsCapture.getValue();

        assertThat(embeds).isEqualTo(mockEmbeds);
    }

    @Test
    void testReplyMessageEmbedError() {
        assertThatCode(() -> this.abstractCommand.reply((MessageEmbed) null))
            .isInstanceOf(OutsideOfCommandContextException.class);
    }

}
