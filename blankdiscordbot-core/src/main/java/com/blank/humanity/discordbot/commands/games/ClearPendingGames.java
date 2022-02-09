package com.blank.humanity.discordbot.commands.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.database.GameMetadataDao;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractHiddenCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericFormatDataKey;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericMessageType;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class ClearPendingGames extends AbstractHiddenCommand {

    public ClearPendingGames() {
	super("clearpendinggames");
    }

    @Autowired
    private GameMetadataDao gameMetadataDao;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	gameMetadataDao
		.findAll()
		.stream()
		.filter((metadata) -> !metadata.isGameFinished())
		.forEach((metadata) -> metadata.setGameFinished(true));

	reply(event,
		FormattingData
			.builder()
			.messageType(GenericMessageType.ERROR_MESSAGE)
			.dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
				"Cleared all pending games!")
			.build());
    }

}
