package de.zorro909.blank.BlankDiscordBot.commands.economy;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class GiveCoinsCommand extends AbstractCommand {

    public GiveCoinsCommand() {
	super("give-coins");
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
				MessageType.GIVE_COINS_COMMAND)
			.dataPairing(FormatDataKey.REWARD_AMOUNT, amount)
			.build());
    }

}
