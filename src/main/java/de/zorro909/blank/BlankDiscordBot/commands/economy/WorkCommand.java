package de.zorro909.blank.BlankDiscordBot.commands.economy;

import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.user.ClaimDataType;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class WorkCommand extends AbstractCommand {

    public WorkCommand() {
	super("work");
    }

    @Override
    protected CommandData createCommandData(CommandData workCommand) {
	return workCommand;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser blankUser = blankUserService.getUser(event);

	FormattingData formattingData = blankUserService
		.claimReward(blankUser, ClaimDataType.WORK_CLAIM)
		.build();

	if (formattingData.success()) {
	    reply(event, formattingData
		    .messageType(MessageType.WORK_COMMAND_MESSAGE));
	} else {
	    reply(event, formattingData
		    .messageType(
			    MessageType.WORK_COMMAND_ALREADY_CLAIMED_MESSAGE));
	}
    }
}
