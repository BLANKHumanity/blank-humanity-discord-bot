package com.blank.humanity.discordbot.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.config.DiscordBotConfig;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.config.messages.MessagesConfig;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.exceptions.command.OutsideOfCommandContextException;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.CommandService;
import com.blank.humanity.discordbot.services.MenuService;
import com.blank.humanity.discordbot.services.MessageService;
import com.blank.humanity.discordbot.services.TransactionExecutor;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.Wrapper;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.internal.utils.Checks;

/**
 * Base Class for all Commands and Games. Manages registration and listening for
 * the command, as well as sending the reply.
 * 
 * @author zorro
 */
@Component
@Getter
@Slf4j
public abstract class AbstractCommand extends ListenerAdapter {

    @Autowired
    protected JDA jda;

    @Autowired
    protected BlankUserService blankUserService;

    @Autowired
    protected MessagesConfig messagesConfig;

    @Autowired
    private TransactionExecutor transactionExecutor;

    @Autowired
    private CommandService commandService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private MessageService messageService;

    private static ThreadLocal<GenericCommandInteractionEvent> commandEvent = new ThreadLocal<>();

    private static ThreadLocal<CommandAutoCompleteInteractionEvent> autoCompleteEvent = new ThreadLocal<>();

    private static ThreadLocal<BlankUser> localUser = new ThreadLocal<>();

    private static ThreadLocal<Member> localMember = new ThreadLocal<>();

    private static ThreadLocal<MessageEmbed[]> localEmbedsToSend = new ThreadLocal<>();

    private static ThreadLocal<DiscordMenu> localMenu = new ThreadLocal<>();

    private static ThreadLocal<Runnable> localCachedTasks = new ThreadLocal<>();

    public Type getCommandType() {
        return Type.SLASH;
    }

    /**
     * Waits for JDA to get ready and then sets up and registers this command to
     * Discord.<br>
     * Will be called automatically via {@link PostConstruct}.
     * 
     * @throws InterruptedException If this thread is interrupted while waiting
     */
    @PostConstruct
    void setupCommand() throws InterruptedException {
        commandService.registerCommand(this);
    }

    /**
     * Specifies the command name of this command class.<br>
     * The name needs to always be lower-case, which is a restriction of
     * Discord.
     * 
     * @return The command name.
     */
    public abstract String getCommandName();

    /**
     * Creates CommandData including subcommands and arguments etc. to be
     * registered.<br>
     * Descriptions can be fetched via
     * {@linkplain #getCommandDefinition()}.{@linkplain CommandDefinition#getOptionDescription(String)
     * getOptionDescription(String)}
     * 
     * @param commandData Already initialized CommandData Object. Command name
     *                    and description is already pre-filled.
     * @param definition  {@link CommandDefinition} for configurable option
     *                    names and descriptions
     * @return The modified CommandData
     */
    public abstract CommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition);

    protected void setUser(BlankUser newUser) {
        localUser.set(newUser);
    }

    protected BlankUser getUser() {
        return localUser.get();
    }

    protected GenericCommandInteractionEvent getCommandEvent() {
        return commandEvent.get();
    }

    protected CommandAutoCompleteInteractionEvent getAutoCompleteEvent() {
        return autoCompleteEvent.get();
    }

    protected Member getMember() {
        return localMember.get();
    }

    protected void setMember(Member member) {
        localMember.set(member);
    }

    protected MessageEmbed[] getUnsentReply() {
        return localEmbedsToSend.get();
    }

    /**
     * @see AbstractHiddenCommand
     * @return True if command should always be hidden.
     */
    public boolean isEphemeral() {
        return false;
    }

    protected void clearThreadLocals() {
        commandEvent.remove();
        autoCompleteEvent.remove();
        localUser.remove();
        localEmbedsToSend.remove();
        localCachedTasks.remove();
        localMenu.remove();
        localMember.remove();
    }

    public Boolean receiveCommandInteraction(
        GenericCommandInteractionEvent interactionEvent) {
        try {
            commandEvent.set(interactionEvent);
            localUser
                .set(blankUserService.getUser(interactionEvent.getMember()));
            localMember.set(interactionEvent.getMember());

            onCommand(interactionEvent);
            return true;
        } catch (Exception exception) {
            receiveCommandInteractionExceptionHandler(exception);
            return false;
        } finally {
            receiveCommandInteractionFinishHandler();

            clearThreadLocals();
        }
    }

    public Boolean receiveAutoCompleteInteraction(
        CommandAutoCompleteInteractionEvent interactionEvent) {
        try {
            autoCompleteEvent.set(interactionEvent);
            localUser
                .set(blankUserService.getUser(interactionEvent.getMember()));
            localMember.set(interactionEvent.getMember());

            Collection<Command.Choice> choices = onAutoComplete(
                interactionEvent);
            interactionEvent.replyChoices(choices).complete();
            return true;
        } catch (Exception exception) {
            log
                .error("Auto Complete Interaction of Command '"
                    + getCommandName() + "' has thrown an exception",
                    exception);
            return false;
        } finally {
            clearThreadLocals();
        }
    }

    /**
     * Handles sending of replies and menus, as well as starting long running
     * Tasks after the {@link #onCommand(InteractionEvent)} Call.<br>
     * If no reply has been set via {@link #reply}, then this will send a error
     * message to notify the user.
     */
    private void receiveCommandInteractionFinishHandler() {
        if (localEmbedsToSend.get() == null) {
            sendErrorMessage("This command somehow didn't respond!");
        }

        WebhookMessageUpdateAction<Message> messageUpdateAction = getCommandEvent()
            .getHook()
            .editOriginalEmbeds(localEmbedsToSend.get());

        if (localMenu.get() != null) {
            DiscordMenu newMenu = localMenu.get();

            Message message = messageUpdateAction.complete();

            newMenu.buildMenu(getJda(), message, menuService);
        } else {
            messageUpdateAction.complete();
        }

        if (localCachedTasks.get() != null) {
            localCachedTasks.get().run();
        }
    }

    /**
     * Any Exception that is thrown during the
     * {@link #onCommand(InteractionEvent)} Call is getting logged here and the
     * user is notified with a Error Message.
     * 
     * @param e The exception that was thrown.
     */
    private void receiveCommandInteractionExceptionHandler(
        @NonNull Exception e) {
        log.error("Transaction threw Exception", e);
        sendErrorMessage(
            "This command threw this error '" + e.getMessage() + "'");
    }

    /**
     * Builds a Message of Type ERROR_MESSAGE with the given
     * {@code errorMessage}<br>
     * It is a Utility Function wrapping a {@link #reply(FormattingData)} call.
     * 
     * @param errorMessage The actual error message
     */
    protected void sendErrorMessage(@NonNull String errorMessage) {
        reply(FormattingData
            .builder()
            .messageType(GenericMessageType.ERROR_MESSAGE)
            .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                errorMessage)
            .build());
    }

    /**
     * Builds Messages from {@link FormattingData} and sets them as the reply
     * that will be sent out from this command invocation.<br>
     * Notice: Replies are only sent out after the command has finished
     * execution, calling this function twice, will cause your first reply to be
     * discarded.<br>
     * If you need direct control over generated MessageEmbeds, use
     * {@link #reply(SlashCommandInteraction, MessageEmbed...)}
     * 
     * @param event          The SlashCommandEvent that is to be replied to
     * @param formattingData The {@link FormattingData}s that describe the
     *                       replies.
     */
    protected void reply(@NonNull FormattingData... formattingDatas) {
        if (getUser() == null)
            throw new OutsideOfCommandContextException(
                "reply(FormattingData) can only be called during Command Execution");

        MessageEmbed[] embeds = Arrays
            .stream(formattingDatas)
            .map(messageService::format)
            .map(message -> new EmbedBuilder().setDescription(message))
            .map(EmbedBuilder::build)
            .toArray(size -> new MessageEmbed[size]);

        localEmbedsToSend.set(embeds);
    }

    /**
     * Sets the {@code embeds} as the replies that will be sent out from this
     * command invocation.<br>
     * Notice: Replies are only sent out after the command has finished
     * execution, calling this function twice, will cause your first reply to be
     * discarded.<br>
     * Using {@linkplain #reply(FormattingData...)} is preferred, since it
     * forces you to use configurable Messages.
     * 
     * @param embeds The {@link MessageEmbed}s that should be sent out.
     */
    protected void reply(@NonNull MessageEmbed... embeds) {
        if (getUser() == null)
            throw new OutsideOfCommandContextException(
                "reply(MessageEmbed) can only be called during Command Execution");

        Checks.noneNull(embeds, "MessageEmbeds");
        localEmbedsToSend.set(embeds);
    }

    /**
     * Adds a {@linkplain MessageMenu} that will be added to the message after
     * the command has finished execution.<br>
     * Notice: Calling this function twice will discard the earlier set
     * ReactionMenu.
     * 
     * @param event       The {@linkplain SlashCommandInteraction} that the
     *                    ReactionMenu should be added to.
     * @param discordMenu The {@linkplain DiscordMenu} to be added.
     */
    protected void addMenu(@NonNull DiscordMenu discordMenu) {
        if (getUser() == null)
            throw new OutsideOfCommandContextException(
                "addMenu(DiscordMenu) can only be called during Command Execution");

        localMenu.set(discordMenu);
    }

    /**
     * Adds a Long Running Task to this command invocation that should be
     * started, once the main command execution has finished.<br>
     * This is highly recommended for any Commands that could take longer than
     * 30 seconds to execute.<br>
     * The {@code task} can call updateMessages as often as it wants to, to
     * update the message with the current data.<br>
     * It is recommended to notify the user that this command invocation can
     * take longer to finish.<br>
     * Notice: Calling this function twice will discard the earlier set Task.
     * 
     * @param task The {@linkplain Subtask} that should be executed.
     */
    protected void addLongRunningTask(@NonNull Subtask task) {
        if (getUser() == null)
            throw new OutsideOfCommandContextException(
                "addLongRunningTask(Subtask) can only be called during Command Execution");

        InteractionHook hook = getCommandEvent().getHook();
        Consumer<FormattingData[]> updateMessages = messages -> hook
            .editOriginalEmbeds(Stream
                .of(messageService.format(messages))
                .map(msg -> new EmbedBuilder().setDescription(msg))
                .map(EmbedBuilder::build)
                .toList())
            .queue();

        Runnable run = () -> transactionExecutor
            .executeAsTransaction(Wrapper
                .transactionCallback(Wrapper
                    .supplyOut(Wrapper.wrap(task, updateMessages),
                        null)),
                Exception::printStackTrace, t -> {
                });

        localCachedTasks.set(run);
    }

    /**
     * Executes the actual command, will only be called if {@code event} matches
     * this class's command.<br>
     * The various protected Methods of this class can be used to reply to this
     * command invocation.
     * 
     * @param event The {@linkplain GenericCommandInteractionEvent} that caused
     *              this command invocation.
     * @see #reply(SlashCommandEvent, FormattingData...)
     * @see #sendErrorMessage(SlashCommandEvent, String)
     * @see #addReactionMenu(SlashCommandEvent, MessageMenu)
     * @see #addLongRunningTask(SlashCommandEvent, Subtask)
     */
    protected abstract void onCommand(
        @NonNull GenericCommandInteractionEvent event);

    @NonNull
    protected Collection<Command.Choice> onAutoComplete(
        @NonNull CommandAutoCompleteInteractionEvent autoCompleteEvent) {
        return Collections.emptyList();
    }
}
