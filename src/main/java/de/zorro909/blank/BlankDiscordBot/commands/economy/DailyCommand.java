package de.zorro909.blank.BlankDiscordBot.commands.economy;

import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.ClaimDataType;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class DailyCommand extends AbstractCommand {

    private final Long dayMilliSeconds = 24L * 60L * 60L * 1000L;

    @Override
    protected CommandData createCommandData() {
	CommandData dailyCommand = new CommandData("daily",
		"Gives daily Income");
	return dailyCommand;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser blankUser = blankUserService.getUser(event);

	FormattingData formattingData = blankUserService
		.claimReward(blankUser, ClaimDataType.DAILY_CLAIM)
		.build();

	if (formattingData.success()) {
	    if (formattingData
		    .containsKey(FormatDataKey.CLAIM_STREAK.getKey())) {
		reply(event, formattingData
			.messageType(MessageType.DAILY_COMMAND_MESSAGE_STREAK));
	    } else {
		reply(event, formattingData
			.messageType(MessageType.DAILY_COMMAND_MESSAGE));
	    }
	} else {
	    reply(event, formattingData
		    .messageType(
			    MessageType.DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE));
	}
    }
}
