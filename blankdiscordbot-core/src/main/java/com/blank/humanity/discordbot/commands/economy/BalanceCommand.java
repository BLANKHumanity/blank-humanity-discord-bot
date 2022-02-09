package com.blank.humanity.discordbot.commands.economy;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class BalanceCommand extends AbstractCommand {

    public BalanceCommand() {
	super("balance");
    }

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData
		.addOption(OptionType.USER, "user",
			getCommandDefinition().getOptionDescription("user"),
			false);
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	OptionMapping option = event.getOption("user");
	Member member;
	if (option == null) {
	    member = event.getMember();
	} else {
	    member = option.getAsMember();
	}

	BlankUser blankUser = blankUserService
		.getUser(member.getIdLong(), member.getGuild().getIdLong());
	int balance = blankUser.getBalance();

	reply(event,
		blankUserService
			.createFormattingData(blankUser,
				EconomyMessageType.BALANCE_COMMAND_MESSAGE)
			.dataPairing(EconomyFormatDataKey.BALANCE, balance)
			.build());
    }

}
