package de.zorro909.blank.BlankDiscordBot.commands.games;

import java.util.Random;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameMetadata;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.menu.ReactionMenu;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class RockPaperScissorsGameCommand extends AbstractGame {

    private char[][] resultMap = { { 't', 'w', 'l' }, { 'l', 't', 'w' },
	    { 'w', 'l', 't' } };

    public RockPaperScissorsGameCommand() {
	super(GameType.ROCK_PAPER_SCISSORS);
    }

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	OptionData betAmount = new OptionData(OptionType.INTEGER, "bet",
		getCommandDefinition().getOptionDescription("bet"), true);
	betAmount.setMinValue(1);
	betAmount.setMaxValue(getCommandConfig().getMaxGameBetAmount());
	OptionData selection = new OptionData(OptionType.STRING, "choice",
		getCommandDefinition().getOptionDescription("choice"), true);
	selection.addChoice("🇷", "rock");
	selection.addChoice("🇵", "paper");
	selection.addChoice("🇸", "scissors");
	commandData.addOptions(betAmount, selection);
	return commandData;
    }

    @Override
    protected ReactionMenu onGameStart(SlashCommandEvent event, BlankUser user,
	    GameMetadata metadata) {
	int betAmount = (int) event.getOption("bet").getAsLong();
	if (betAmount > user.getBalance()) {
	    reply(event,
		    getBlankUserService()
			    .createFormattingData(user,
				    MessageType.GAME_BET_NOT_ENOUGH_MONEY)
			    .dataPairing(FormatDataKey.BET_AMOUNT, betAmount)
			    .build());
	    abort(metadata);
	}

	int opponentRoll = new Random().nextInt(3);

	int userSelection = selectionToInt(
		event.getOption("choice").getAsString());

	char result = resultMap[opponentRoll][userSelection];

	switch (result) {
	case 't' -> tie(event, user, betAmount, userSelection, opponentRoll);
	case 'w' -> win(event, user, betAmount, userSelection, opponentRoll);
	case 'l' -> loss(event, user, betAmount, userSelection, opponentRoll);
	}

	finish(metadata);
	return null;
    }

    private int selectionToInt(String option) {
	return switch (option) {
	case "rock" -> 0;
	case "paper" -> 1;
	case "scissors" -> 2;
	default -> throw new IllegalArgumentException(
		"Unexpected value: " + option);
	};
    }

    private String intToSelection(int selection) {
	return switch (selection) {
	case 0 -> "🪨";
	case 1 -> "📰";
	case 2 -> "✂";
	default -> throw new IllegalArgumentException(
		"Unexpected value: " + selection);
	};
    }

    private void loss(SlashCommandEvent event, BlankUser user, int betAmount,
	    int userSel, int botSel) {
	getBlankUserService().decreaseUserBalance(user, betAmount);

	FormattingData tie = getBlankUserService()
		.createFormattingData(user,
			MessageType.ROCK_PAPER_SCISSORS_LOSS)
		.dataPairing(FormatDataKey.BET_AMOUNT, betAmount)
		.dataPairing(FormatDataKey.RPS_USER, intToSelection(userSel))
		.dataPairing(FormatDataKey.RPS_BOT, intToSelection(botSel))
		.build();

	reply(event, tie);
    }

    private void win(SlashCommandEvent event, BlankUser user, int betAmount,
	    int userSel, int botSel) {
	getBlankUserService().increaseUserBalance(user, betAmount);

	FormattingData tie = getBlankUserService()
		.createFormattingData(user, MessageType.ROCK_PAPER_SCISSORS_WIN)
		.dataPairing(FormatDataKey.BET_AMOUNT, betAmount)
		.dataPairing(FormatDataKey.RPS_USER, intToSelection(userSel))
		.dataPairing(FormatDataKey.RPS_BOT, intToSelection(botSel))
		.build();

	reply(event, tie);
    }

    private void tie(SlashCommandEvent event, BlankUser user, int betAmount,
	    int userSel, int botSel) {
	FormattingData tie = getBlankUserService()
		.createFormattingData(user, MessageType.ROCK_PAPER_SCISSORS_TIE)
		.dataPairing(FormatDataKey.BET_AMOUNT, betAmount)
		.dataPairing(FormatDataKey.RPS_USER, intToSelection(userSel))
		.dataPairing(FormatDataKey.RPS_BOT, intToSelection(botSel))
		.build();

	reply(event, tie);
    }

    @Override
    protected ReactionMenu onGameContinue(BlankUser user, GameMetadata metadata,
	    Object argument, Consumer<FormattingData> messageEdit) {
	return null;
    }

}
