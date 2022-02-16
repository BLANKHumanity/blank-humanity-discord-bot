package com.blank.humanity.discordbot.commands.economy;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class GiveCoinsCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "give-coins";
    }

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData
		.addOption(OptionType.USER, "user",
			getCommandDefinition().getOptionDescription("user"),
			true);
	commandData
		.addOption(OptionType.INTEGER, "amount",
			getCommandDefinition().getOptionDescription("amount"),
			true);
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	Member receiver = event.getOption("user").getAsMember();
	BlankUser receivingUser = getBlankUserService().getUser(receiver);

	int amount = (int) event.getOption("amount").getAsLong();

	getBlankUserService().increaseUserBalance(receivingUser, amount);
	reply(event,
		getBlankUserService()
			.createFormattingData(receivingUser,
				EconomyMessageType.GIVE_COINS_COMMAND)
			.dataPairing(EconomyFormatDataKey.REWARD_AMOUNT, amount)
			.build());
    }

}
