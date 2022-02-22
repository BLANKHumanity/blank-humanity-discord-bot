package com.blank.humanity.discordbot.commands.economy;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.ClaimDataType;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class DailyCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "daily";
    }

    @Override
    protected SlashCommandData createCommandData(SlashCommandData dailyCommand) {
	return dailyCommand;
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
	BlankUser blankUser = blankUserService.getUser(event);

	FormattingData formattingData = blankUserService
		.claimReward(blankUser, ClaimDataType.DAILY_CLAIM)
		.build();

	if (formattingData.success()) {
	    if (formattingData.containsKey(EconomyFormatDataKey.CLAIM_STREAK)) {
		reply(event, formattingData
			.messageType(
				EconomyMessageType.DAILY_COMMAND_MESSAGE_STREAK));
	    } else {
		reply(event, formattingData
			.messageType(EconomyMessageType.DAILY_COMMAND_MESSAGE));
	    }
	} else {
	    reply(event, formattingData
		    .messageType(
			    EconomyMessageType.DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE));
	}
    }
}
