package com.blank.humanity.discordbot.commands;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
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
import com.blank.humanity.discordbot.config.messages.MessagesConfig;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.TransactionExecutor;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.NamedFormatter;
import com.blank.humanity.discordbot.utils.Wrapper;
import com.blank.humanity.discordbot.utils.menu.ReactionMenu;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.internal.utils.Checks;

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

    private CommandData commandData;

    private String commandName;

    private CommandDefinition commandDefinition;

    private static HashMap<SlashCommandEvent, MessageEmbed[]> cachedEmbeds = new HashMap<>();

    private static HashMap<SlashCommandEvent, ReactionMenu> cachedMenus = new HashMap<>();

    private static HashMap<SlashCommandEvent, Runnable> cachedTasks = new HashMap<>();

    protected AbstractCommand(String command) {
	this.commandName = command;
    }

    @PostConstruct
    void setupCommand() {
	try {
	    jda.awaitReady();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	updateCommandDefinition();

	jda.addEventListener(this);
	
	log.info("Registered Command '" + getCommandName() + "'");
    }

    public void updateCommandDefinition() {
	this.commandDefinition = commandConfig
		.getCommandDefinition(commandName);
	commandData = createCommandData(new CommandData(commandName,
		getCommandDefinition().getDescription()));

	Guild guild = jda.getGuildById(commandConfig.getGuildId());

	if (commandDefinition.isRoleRestricted()) {
	    commandData.setDefaultEnabled(false);
	    guild.upsertCommand(commandData).queue(command -> {
		guild
			.updateCommandPrivilegesById(command.getIdLong(),
				commandDefinition
					.getAllowedRoles()
					.stream()
					.map(CommandPrivilege::enableRole)
					.toList())
			.queue();
	    });
	} else {
	    guild.upsertCommand(commandData).queue();
	}
    }

    protected abstract CommandData createCommandData(CommandData commandData);

    protected boolean isEphemeral() {
	return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
	if (commandData.getName().equals(event.getName())) {
	    boolean hidden = isEphemeral() || commandDefinition.isHidden()
		    || isChannelHidden(event.getChannel().getIdLong());
	    event.deferReply(hidden).queue();

	    transactionExecutor
		    .executeAsTransaction(
			    Wrapper
				    .transactionCallback(Wrapper
					    .supplyOut(Wrapper
						    .wrap(this::onCommand,
							    event),
						    null)),
			    ex -> transactionExceptionHandler(event, ex),
			    o -> transactionFinishHandler(event));

	}
    }

    private void transactionFinishHandler(SlashCommandEvent event) {
	if (event == null) {
	    System.out.println("Event is null!!!");
	}
	System.out.println("Event ID:" + event.getIdLong());
	if (!cachedEmbeds.containsKey(event)) {
	    sendErrorMessage(event, "This Command somehow didn't respond!");
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
	    System.out.println("Running Long running Task");
	    cachedTasks.remove(event).run();
	}
    }

    private void transactionExceptionHandler(SlashCommandEvent event,
	    Exception e) {
	e.printStackTrace();
	sendErrorMessage(event,
		"This Command threw this error '" + e.getMessage() + "'");
    }

    protected void sendErrorMessage(SlashCommandEvent event, String message) {
	reply(event,
		FormattingData
			.builder()
			.messageType(GenericMessageType.ERROR_MESSAGE)
			.dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
				message)
			.build());
    }

    private boolean isChannelHidden(long channelId) {
	return commandConfig.getHiddenCommandChannels().contains(channelId);
    }

    protected void reply(SlashCommandEvent event,
	    @Valid FormattingData formattingData) {
	EmbedBuilder builder = new EmbedBuilder();
	builder.setDescription(format(formattingData));

	cachedEmbeds.put(event, new MessageEmbed[] { builder.build() });
    }

    protected void reply(SlashCommandEvent event, MessageEmbed... embeds) {
	Checks.noneNull(embeds, "MessageEmbeds");
	cachedEmbeds.put(event, embeds);
    }

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

    protected void addReactionMenu(SlashCommandEvent event,
	    ReactionMenu reactionMenu) {
	cachedMenus.put(event, reactionMenu);
    }

    protected void addLongRunningTask(SlashCommandEvent event, Subtask task) {
	Consumer<FormattingData[]> updateMessages = messages -> event
		.getHook()
		.editOriginalEmbeds(Stream
			.of(messages)
			.map(this::format)
			.map((msg) -> new EmbedBuilder().setDescription(msg))
			.map(EmbedBuilder::build)
			.toList())
		.queue();

	Runnable run = () -> transactionExecutor
		.executeAsTransaction(Wrapper
			.transactionCallback(Wrapper
				.supplyOut(Wrapper.wrap(task, updateMessages),
					null)),
			Exception::printStackTrace, (t) -> {
			});

	cachedTasks.put(event, run);
    }

    protected abstract void onCommand(SlashCommandEvent event);
}
