package de.zorro909.blank.BlankDiscordBot.commands.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractHiddenCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.database.GameMetadataDao;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
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
			.messageType(MessageType.ERROR_MESSAGE)
			.dataPairing(FormatDataKey.ERROR_MESSAGE,
				"Cleared all pending games!")
			.build());
    }

}