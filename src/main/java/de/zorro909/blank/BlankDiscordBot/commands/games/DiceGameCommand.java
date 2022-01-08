package de.zorro909.blank.BlankDiscordBot.commands.games;

import java.util.Random;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameMetadata;
import de.zorro909.blank.BlankDiscordBot.entities.game.GameType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.user.fake.FakeUser;
import de.zorro909.blank.BlankDiscordBot.entities.user.fake.FakeUserType;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.menu.ReactionMenu;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class DiceGameCommand extends AbstractGame {

    public DiceGameCommand() {
	super(GameType.DICE);
    }

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	OptionData bet = new OptionData(OptionType.INTEGER, "bet",
		getCommandDefinition().getOptionDescription("bet"), true);
	bet.setMinValue(1);
	bet.setMaxValue(getCommandConfig().getMaxGameBetAmount());
	commandData.addOptions(bet);
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

	getBlankUserService().decreaseUserBalance(user, betAmount);

	FakeUser diceJackpot = FakeUserType.DICE_JACKPOT
		.getFakeUser(getBlankUserService());

	diceJackpot.increaseBalance((int) Math.round(betAmount / 20d));

	DiceRoll playerRoll = diceRoll();
	DiceRoll opponentRoll = diceRoll();

	MessageType messageType;
	int reward = 0;

	if (playerRoll.sum() > opponentRoll.sum()) {
	    if (playerRoll.isSnakeEyes()) {
		messageType = MessageType.DICE_GAME_JACKPOT;

		reward = betAmount + diceJackpot.getBalance();
		diceJackpot.decreaseBalance(diceJackpot.getBalance());
	    } else {
		messageType = MessageType.DICE_GAME_WIN;
		reward = betAmount;
	    }
	    getBlankUserService().increaseUserBalance(user, betAmount + reward);
	} else {
	    messageType = MessageType.DICE_GAME_LOSS;
	}

	reply(event,
		getBlankUserService()
			.createFormattingData(user, messageType)
			.dataPairing(FormatDataKey.BET_AMOUNT, betAmount)
			.dataPairing(FormatDataKey.REWARD_AMOUNT, reward)
			.dataPairing(FormatDataKey.DICE_ROLL_USER,
				playerRoll.toString())
			.dataPairing(FormatDataKey.DICE_ROLL_OPPONENT,
				opponentRoll.toString())
			.build());
	return null;
    }

    private DiceRoll diceRoll() {
	Random rand = new Random();
	return new DiceRoll(rand.nextInt(6), rand.nextInt(6));
    }

    @Override
    protected ReactionMenu onGameContinue(BlankUser user, GameMetadata metadata,
	    Object argument, Consumer<FormattingData> messageEdit) {
	return null;
    }

    private record DiceRoll(int roll1, int roll2) {

	@Override
	public String toString() {
	    return intToEmoji(roll1) + " " + intToEmoji(roll2);
	}

	public int sum() {
	    return roll1 + roll2;
	}

	public boolean isSnakeEyes() {
	    return roll1 == roll2;
	}

	private String intToEmoji(int number) {
	    return switch (number) {
	    case 1 -> ":one:";
	    case 2 -> ":two:";
	    case 3 -> ":three:";
	    case 4 -> ":four:";
	    case 5 -> ":five:";
	    case 6 -> ":six:";
	    default -> ":interrobang: (Something bad happened)";
	    };
	}

    }

}
