package de.zorro909.blank.BlankDiscordBot.commands.economy;

import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
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
				MessageType.BALANCE_COMMAND_MESSAGE)
			.dataPairing(FormatDataKey.BALANCE, balance)
			.build());
    }

}
