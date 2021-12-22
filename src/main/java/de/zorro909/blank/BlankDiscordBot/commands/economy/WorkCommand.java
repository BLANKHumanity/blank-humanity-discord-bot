package de.zorro909.blank.BlankDiscordBot.commands.economy;

import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.ClaimDataType;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class WorkCommand extends AbstractCommand {

    private final Long hourMilliSeconds = 60L * 60L * 1000L;

    @Override
    protected CommandData createCommandData() {
	CommandData workCommand = new CommandData("work",
		"Lets you do work hourly!");
	return workCommand;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser blankUser = blankUserService.getUser(event);

	FormattingData formattingData = blankUserService
		.claimReward(blankUser, ClaimDataType.WORK_CLAIM,
			hourMilliSeconds, Long.MAX_VALUE)
		.build();

	if (formattingData.isSuccess()) {
	    reply(event, messagesConfig.WORK_COMMAND_MESSAGE, formattingData);
	} else {
	    reply(event, messagesConfig.WORK_COMMAND_ALREADY_CLAIMED_MESSAGE,
		    formattingData);
	}
    }
}
