package com.blank.humanity.discordbot.commands;

import static com.blank.humanity.discordbot.utils.Wrapper.transactionCallback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.config.DiscordBotConfig;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.config.messages.MessagesConfig;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.TransactionExecutor;
import com.blank.humanity.discordbot.utils.FormatDataKey;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.NamedFormatter;
import com.blank.humanity.discordbot.utils.Wrapper;
import com.blank.humanity.discordbot.utils.menu.ReactionMenu;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
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
    Validator validator;

    @Autowired
    protected JDA jda;

    @Autowired
    protected BlankUserService blankUserService;

    @Autowired
    protected MessagesConfig messagesConfig;

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private DiscordBotConfig discordBotConfig;

    @Autowired
    private TransactionExecutor transactionExecutor;

    @Autowired
    private Environment environment;

    @Autowired
    private TaskScheduler taskScheduler;

    private SlashCommandData commandData;

    private CommandDefinition commandDefinition;

    private static Map<SlashCommandInteraction, MessageEmbed[]> cachedEmbeds = new HashMap<>();

    private static Map<SlashCommandInteraction, ReactionMenu> cachedMenus = new HashMap<>();

    private static Map<SlashCommandInteraction, Runnable> cachedTasks = new HashMap<>();

    /**
     * Waits for JDA to get ready and then sets up and registers this command to
     * Discord.<br>
     * Will be called automatically via {@link PostConstruct}.
     * 
     * @throws InterruptedException If this thread is interrupted while waiting
     */
    @PostConstruct
    void setupCommand() throws InterruptedException {
        jda.awaitReady();

        updateCommandDefinition();

        jda.addEventListener(this);

        log.info("Registered Command '" + getCommandName() + "'");
    }

    /**
     * Registers or updates this Command's Data with Discord.<br>
     * Can be called manually, if {@link #createCommandData(CommandData)}
     * changes. (Example: changing subcommands)
     */
    public void updateCommandDefinition() {
        this.commandDefinition = commandConfig
            .getCommandDefinition(getCommandName());
        commandData = createCommandData(Commands
            .slash(getCommandName(),
                getCommandDefinition().getDescription()));

        Guild guild = jda.getGuildById(commandConfig.getGuildId());

        if (commandDefinition.isRoleRestricted()) {
            commandData.setDefaultEnabled(false);
            guild
                .upsertCommand(commandData)
                .queue(command -> guild
                    .updateCommandPrivilegesById(command.getIdLong(),
                        commandDefinition
                            .getAllowedRoles()
                            .stream()
                            .map(CommandPrivilege::enableRole)
                            .toList())
                    .queue());
        } else {
            guild.upsertCommand(commandData).queue();
        }
    }

    /**
     * Creates CommandData including subcommands and arguments etc. to be
     * registered.<br>
     * Descriptions can be fetched via
     * {@linkplain #getCommandDefinition()}.{@linkplain CommandDefinition#getOptionDescription(String)
     * getOptionDescription(String)}
     * 
     * @param commandData Already initialized CommandData Object. Command name
     *                    and description is already pre-filled.
     * @return The modified CommandData
     */
    protected abstract SlashCommandData createCommandData(
        SlashCommandData commandData);

    /**
     * @see AbstractHiddenCommand
     * @return True if command should always be hidden.
     */
    protected boolean isEphemeral() {
        return false;
    }

    /**
     * Listens for {@linkplain SlashCommandEvent}s and calls
     * {@linkplain #onCommand(SlashCommandEvent)} if the event belongs to this
     * command.<br>
     * Also hides the reply automatically, if either
     * {@linkplain #isEphemeral()}, {@linkplain CommandDefinition#isHidden()
     * getCommandDefinition().isHidden()} or {@linkplain #isChannelHidden(long)}
     * returns true.
     * 
     * @param event The SlashCommandEvent from Discord
     */
    @Override
    public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) {
        if (commandData.getName().equals(event.getName())) {
            boolean hidden = isEphemeral() || commandDefinition.isHidden()
                || isChannelHidden(event.getChannel().getIdLong());
            event.deferReply(hidden).queue();
            
            transactionExecutor
                .executeAsTransaction(
                    transactionCallback(
                        () -> onCommand(event)),
                    ex -> transactionExceptionHandler(event, ex),
                    o -> transactionFinishHandler(event));

        }
    }

    /**
     * Handles sending of replies and menus, as well as starting long running
     * Tasks after the {@link #onCommand(SlashCommandInteraction)} Call.<br>
     * If no reply has been set via {@link #reply}, then this will send a error
     * message to notify the user.
     * 
     * @param event The Command Event that needs to be finished
     */
    private void transactionFinishHandler(
        @NonNull SlashCommandInteraction event) {
        if (!cachedEmbeds.containsKey(event)) {
            sendErrorMessage(event, "This command somehow didn't respond!");
        }

        Message message = event
            .getHook()
            .editOriginalEmbeds(cachedEmbeds.remove(event))
            .complete();
        if (cachedMenus.containsKey(event)) {
            cachedMenus
                .remove(event)
                .buildMenu(getJda(), message, getTaskScheduler(),
                    getTransactionExecutor());
        }
        if (cachedTasks.containsKey(event)) {
            cachedTasks.remove(event).run();
        }
    }

    /**
     * Any Exception that is thrown during the
     * {@link #onCommand(SlashCommandInteraction)} Call is getting logged here and the
     * user is notified with a Error Message.
     * 
     * @param event The event that resulted in an Exception.
     * @param e     The exception that was thrown.
     */
    private void transactionExceptionHandler(@NonNull SlashCommandInteraction event,
        @NonNull Exception e) {
        log.error("Transaction threw Exception", e);
        sendErrorMessage(event,
            "This command threw this error '" + e.getMessage() + "'");
    }

    /**
     * Builds a Message of Type ERROR_MESSAGE with the given
     * {@code errorMessage}<br>
     * It is a Utility Function wrapping a
     * {@link #reply(SlashCommandInteraction, FormattingData)} call.
     * 
     * @param event        The CommandEvent that a error needs to be sent to as
     *                     a reply
     * @param errorMessage The actual error message
     */
    protected void sendErrorMessage(@NonNull SlashCommandInteraction event,
        @NonNull String errorMessage) {
        reply(event,
            FormattingData
                .builder()
                .messageType(GenericMessageType.ERROR_MESSAGE)
                .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                    errorMessage)
                .build());
    }

    /**
     * Determines if the channel with the Id {@code channelId} should be hidden
     * or not.<br>
     * Default behaviour checks the command definition configuration's
     * hiddenCommandChannels.
     * 
     * @param channelId Id of the Channel that should be checked
     * @return True if this command should be hidden in the specified channel.
     */
    private boolean isChannelHidden(long channelId) {
        return commandConfig.getHiddenCommandChannels().contains(channelId);
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
    protected void reply(@NonNull SlashCommandInteraction event,
        @NonNull FormattingData... formattingDatas) {
        MessageEmbed[] embeds = Arrays
            .stream(formattingDatas)
            .map(this::format)
            .map(message -> new EmbedBuilder().setDescription(message))
            .map(EmbedBuilder::build)
            .toArray(size -> new MessageEmbed[size]);

        cachedEmbeds.put(event, embeds);
    }

    /**
     * Sets the {@code embeds} as the replies that will be sent out from this
     * command invocation.<br>
     * Notice: Replies are only sent out after the command has finished
     * execution, calling this function twice, will cause your first reply to be
     * discarded.<br>
     * Using {@linkplain #reply(SlashCommandEvent, FormattingData...)} is
     * preferred, since it forces you to use configurable Messages.
     * 
     * @param event  The SlashCommandInteraction that is to be replied to
     * @param embeds The {@link MessageEmbed}s that should be sent out.
     */
    protected void reply(@NonNull SlashCommandInteraction event,
        @NonNull MessageEmbed... embeds) {
        Checks.noneNull(embeds, "MessageEmbeds");
        cachedEmbeds.put(event, embeds);
    }

    /**
     * Takes in {@linkplain FormattingData} and uses it to generate a formatted
     * Message according to the Configuration of the specified
     * {@linkplain MessageType}.<br>
     * Also checks for Validity of the FormattingData, MessageType can specify
     * {@linkplain FormatDataKey}s that need to be filled in. If any are missing
     * a Format Error Message will be returned.
     * 
     * @param formattingData The {@linkplain FormattingData} that is used to
     *                       generate the Message
     * @return A formatted Message
     */
    protected String format(FormattingData formattingData) {
        Set<ConstraintViolation<FormattingData>> constraintViolation = validator
            .validate(formattingData);
        if (!constraintViolation.isEmpty()) {
            log
                .error("Format Error\n" + constraintViolation
                    .stream()
                    .map(ConstraintViolation<FormattingData>::getMessage)
                    .collect(Collectors.joining("\n")));
            return "Format Error\n" + constraintViolation
                .stream()
                .map(ConstraintViolation<FormattingData>::getMessage)
                .collect(Collectors.joining("\n"));
        }
        String messageFormat = formattingData
            .messageType()
            .getMessageFormat(environment);
        return NamedFormatter
            .namedFormat(messageFormat, formattingData.getDataPairings());
    }

    /**
     * Adds a {@linkplain ReactionMenu} that will be added to the message after
     * the command has finished execution.<br>
     * Notice: Calling this function twice will discard the earlier set
     * ReactionMenu.
     * 
     * @param event        The {@linkplain SlashCommandInteraction} that the
     *                     ReactionMenu should be added to.
     * @param reactionMenu The {@linkplain ReactionMenu} to be added.
     */
    protected void addReactionMenu(@NonNull SlashCommandInteraction event,
        @NonNull ReactionMenu reactionMenu) {
        cachedMenus.put(event, reactionMenu);
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
     * @param event The {@linkplain SlashCommandInteraction} that the Task should be
     *              added to.
     * @param task  The {@linkplain Subtask} that should be executed.
     */
    protected void addLongRunningTask(@NonNull SlashCommandInteraction event,
        @NonNull Subtask task) {
        Consumer<FormattingData[]> updateMessages = messages -> event
            .getHook()
            .editOriginalEmbeds(Stream
                .of(messages)
                .map(this::format)
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

        cachedTasks.put(event, run);
    }

    /**
     * Specifies the command name of this command class.<br>
     * The name needs to always be lower-case, which is a restriction of
     * Discord.
     * 
     * @return The command name.
     */
    protected abstract String getCommandName();

    /**
     * Executes the actual command, will only be called if {@code event} matches
     * this class's command.<br>
     * The various protected Methods of this class can be used to reply to this
     * command invocation.
     * 
     * @param event The {@linkplain SlashCommandInteraction} that caused this command
     *              invocation.
     * @see #reply(SlashCommandEvent, FormattingData...)
     * @see #sendErrorMessage(SlashCommandEvent, String)
     * @see #addReactionMenu(SlashCommandEvent, ReactionMenu)
     * @see #addLongRunningTask(SlashCommandEvent, Subtask)
     */
    protected abstract void onCommand(@NonNull SlashCommandInteraction event);
}
