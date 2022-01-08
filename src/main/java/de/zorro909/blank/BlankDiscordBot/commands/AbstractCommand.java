package de.zorro909.blank.BlankDiscordBot.commands;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.config.DiscordBotConfig;
import de.zorro909.blank.BlankDiscordBot.config.commands.CommandConfig;
import de.zorro909.blank.BlankDiscordBot.config.commands.CommandDefinition;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessagesConfig;
import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;
import de.zorro909.blank.BlankDiscordBot.services.TransactionExecutor;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.NamedFormatter;
import de.zorro909.blank.BlankDiscordBot.utils.menu.ReactionMenu;
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
    private TaskScheduler taskScheduler;

    private CommandData commandData;

    private String commandName;

    private CommandDefinition commandDefinition;

    private static HashMap<SlashCommandEvent, MessageEmbed[]> cachedEmbeds = new HashMap<>();

    private static HashMap<SlashCommandEvent, ReactionMenu> cachedMenus = new HashMap<>();

    public AbstractCommand(String command) {
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
	    event
		    .deferReply(isEphemeral() || commandDefinition.isHidden()
			    || isChannelHidden(event.getChannel().getIdLong()))
		    .queue();

	    transactionExecutor.executeAsTransaction((status) -> {
		onCommand(event);
		return null;
	    }, (e) -> {
		e.printStackTrace();
		reply(event,
			FormattingData
				.builder()
				.messageType(MessageType.ERROR_MESSAGE)
				.dataPairing(FormatDataKey.ERROR_MESSAGE,
					"This Command threw this error '"
						+ e.getMessage() + "'")
				.build());
	    }, (unused) -> {
		if (!cachedEmbeds.containsKey(event)) {
		    reply(event, FormattingData
			    .builder()
			    .messageType(MessageType.ERROR_MESSAGE)
			    .dataPairing(FormatDataKey.ERROR_MESSAGE,
				    "This Command somehow didn't respond!")
			    .build());
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
	    });
	}
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
	String messageFormat = "";
	try {
	    messageFormat = formattingData
		    .messageType()
		    .getMessageFormat(messagesConfig);
	} catch (IllegalArgumentException | IllegalAccessException e) {
	    return e.getMessage();
	}
	return NamedFormatter
		.namedFormat(messageFormat, formattingData.getDataPairings());
    }

    protected void addReactionMenu(SlashCommandEvent event,
	    ReactionMenu reactionMenu) {
	cachedMenus.put(event, reactionMenu);
    }

    protected abstract void onCommand(SlashCommandEvent event);
}
