package de.zorro909.blank.BlankDiscordBot.commands;

import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class BalanceCommand extends AbstractCommand {

    @Override
    protected CommandData createCommandData() {
	CommandData balanceCommand = new CommandData("balance",
		"🪙 /balance [user] - Displays the users current Balance, Job and Daily Income");
	balanceCommand
		.addOption(OptionType.USER, "user",
			"Displays Balance of the specified user", false);
	return balanceCommand;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	OptionMapping option = event.getOption("user");
	User user;
	if (option == null) {
	    user = event.getUser();
	} else {
	    user = option.getAsUser();
	}

	BlankUser blankUser = blankUserService.getUser(user.getIdLong());
	int balance = blankUser.getBalance();

	reply(event, messagesConfig.BALANCE_COMMAND_MESSAGE,
		blankUserService
			.createFormattingData(blankUser)
			.dataPairing(FormatDataKey.BALANCE, balance)
			.build());
    }

}
