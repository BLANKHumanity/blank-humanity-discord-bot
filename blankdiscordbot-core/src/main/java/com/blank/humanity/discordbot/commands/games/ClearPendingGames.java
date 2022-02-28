package com.blank.humanity.discordbot.commands.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.database.GameMetadataDao;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class ClearPendingGames extends AbstractHiddenCommand {

    @Autowired
    private GameMetadataDao gameMetadataDao;

    @Override
    public String getCommandName() {
        return "clearpendinggames";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        gameMetadataDao
            .findAll()
            .stream()
            .filter(metadata -> !metadata.isGameFinished())
            .forEach(metadata -> metadata.setGameFinished(true));

        reply(FormattingData
            .builder()
            .messageType(GenericMessageType.ERROR_MESSAGE)
            .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                "Cleared all pending games!")
            .build());
    }

}
