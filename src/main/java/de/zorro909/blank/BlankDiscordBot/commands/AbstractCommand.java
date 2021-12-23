package de.zorro909.blank.BlankDiscordBot.commands;

import java.sql.Connection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import de.zorro909.blank.BlankDiscordBot.config.DiscordBotConfig;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessagesConfig;
import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.NamedFormatter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

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
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private DiscordBotConfig discordBotConfig;

    private CommandData commandData;

    @PostConstruct
    void setupCommand() {
	commandData = createCommandData();
	try {
	    jda.awaitReady();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	jda
		.getGuildById(556216333201702942L)
		.upsertCommand(commandData)
		.queue();

	jda.addEventListener(this);
    }

    protected abstract CommandData createCommandData();

    protected boolean isEphemeral() {
	return false;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
	if (commandData.getName().equals(event.getName())) {
	    event.deferReply(isEphemeral()).queue();

	    TransactionTemplate txTemplate = new TransactionTemplate(
		    transactionManager);
	    txTemplate
		    .setPropagationBehavior(
			    TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	    txTemplate
		    .setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
	    taskExecutor.execute(() -> {
		txTemplate.executeWithoutResult((status) -> onCommand(event));
	    });
	}
    }

    protected void reply(SlashCommandEvent event,
	    @Valid FormattingData formattingData) {
	EmbedBuilder builder = new EmbedBuilder();
	builder.setDescription(format(formattingData));

	event.getHook().editOriginalEmbeds(builder.build()).queue();
    }

    protected void reply(SlashCommandEvent event, MessageEmbed... embeds) {
	event.getHook().editOriginalEmbeds(embeds).queue();
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

    protected abstract void onCommand(SlashCommandEvent event);
}
