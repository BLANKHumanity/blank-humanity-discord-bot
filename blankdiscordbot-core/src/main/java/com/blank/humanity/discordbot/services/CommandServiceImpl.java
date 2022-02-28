package com.blank.humanity.discordbot.services;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.commands.CommandConfig;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.Wrapper;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

@Slf4j
@Service
public class CommandServiceImpl implements CommandService, EventListener {

    @Autowired
    private CommandConfig commandConfig;

    @Autowired
    private TransactionExecutor transactionExecutor;

    @Autowired
    private MessageService messageService;

    @Autowired
    private JDA jda;

    private Map<String, AbstractCommand> commands = new HashMap<>();

    @PostConstruct
    protected void setupCommandService() {
        jda.addEventListener(this);
    }

    @Override
    public void registerCommand(AbstractCommand command) {
        commands.put(command.getCommandName(), command);

        updateCommand(command.getCommandName());

        log.info("Registered Command '" + command.getCommandName() + "'");
    }

    @Override
    public void updateCommand(String commandName) {
        CommandDefinition commandDefinition = commandConfig
            .getCommandDefinition(commandName);

        AbstractCommand command = commands.get(commandName);

        SlashCommandData commandData = switch (command.getCommandType()) {
        case SLASH -> Commands
            .slash(commandName, commandDefinition.getDescription());
        case USER -> new CommandDataImpl(Type.USER, commandName);
        case MESSAGE -> new CommandDataImpl(Type.MESSAGE, commandName);
        case UNKNOWN -> throw new RuntimeException(
            "Unknown Command Type specified for Command '" + commandName + "'");
        };

        CommandData modifiedCommandData = command
            .createCommandData(commandData, commandDefinition);

        Guild guild = jda.getGuildById(commandConfig.getGuildId());

        if (commandDefinition.isRoleRestricted()) {
            commandData.setDefaultEnabled(false);
            guild
                .upsertCommand(modifiedCommandData)
                .map(Command::getIdLong)
                .flatMap(commandId -> guild
                    .updateCommandPrivilegesById(commandId,
                        commandDefinition
                            .getAllowedRoles()
                            .stream()
                            .map(CommandPrivilege::enableRole)
                            .toList()))
                .queue();
        } else {
            guild.upsertCommand(commandData).queue();
        }
    }

    @Override
    public void receiveInteraction(Interaction interaction) {
        if (interaction instanceof CommandAutoCompleteInteractionEvent autoCompleteEvent) {
            String commandName = autoCompleteEvent.getName();

            if (commands.containsKey(commandName)) {
                transactionExecutor
                    .executeAsTransactionSync(
                        Wrapper
                            .transactionCallback(
                                () -> commands
                                    .get(commandName)
                                    .receiveAutoCompleteInteraction(
                                        autoCompleteEvent)));
            }
        } else if (interaction instanceof GenericCommandInteractionEvent commandInteractionEvent) {
            String commandName = commandInteractionEvent.getName();

            if (commands.containsKey(commandName)) {
                AbstractCommand command = commands.get(commandName);
                long channelId = commandInteractionEvent
                    .getChannel()
                    .getIdLong();
                boolean hidden = isReplyHidden(command, channelId);

                commandInteractionEvent.deferReply(hidden).queue();
                transactionExecutor
                    .executeAsTransaction(
                        status -> command
                            .receiveCommandInteraction(commandInteractionEvent),
                        ex -> transactionExceptionHandler(
                            commandInteractionEvent, ex),
                        success -> transactionFinishHandler(
                            commandInteractionEvent, success));

            }
        }
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof Interaction interaction) {
            receiveInteraction(interaction);
        }
    }

    private boolean isReplyHidden(AbstractCommand command, long channelId) {
        boolean hidden = command.isEphemeral();
        hidden |= commandConfig
            .getCommandDefinition(command.getCommandName())
            .isHidden();
        hidden |= commandConfig.getHiddenCommandChannels().contains(channelId);
        return hidden;
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
        @NonNull GenericCommandInteractionEvent event, Boolean success) {
        if (Boolean.FALSE.equals(success)) {
            FormattingData errorMessage = FormattingData
                .builder()
                .messageType(GenericMessageType.ERROR_MESSAGE)
                .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                    "This command somehow didn't respond!")
                .build();

            event
                .getHook()
                .editOriginal(messageService.format(errorMessage))
                .complete();
        }
    }

    /**
     * Any Exception that is thrown during the
     * {@link #onCommand(SlashCommandInteraction)} Call is getting logged here
     * and the user is notified with a Error Message.
     * 
     * @param event The event that resulted in an Exception.
     * @param e     The exception that was thrown.
     */
    private void transactionExceptionHandler(
        @NonNull GenericCommandInteractionEvent event,
        @NonNull Exception e) {
        log.error("Transaction threw Exception", e);

        FormattingData errorMessage = FormattingData
            .builder()
            .messageType(GenericMessageType.ERROR_MESSAGE)
            .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                e.getMessage())
            .build();

        event
            .getHook()
            .editOriginal(messageService.format(errorMessage))
            .complete();
    }

}
