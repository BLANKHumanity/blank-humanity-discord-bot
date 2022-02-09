package com.blank.humanity.discordbot.commands.games;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.games.messages.GameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GameMessageType;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.game.GameType;
import com.blank.humanity.discordbot.entities.game.RouletteMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.menu.ReactionMenu;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class RouletteGameCommand extends AbstractGame {

    public RouletteGameCommand() {
	super(GameType.ROULETTE);
    }

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	OptionData betAmount = new OptionData(OptionType.INTEGER, "bet",
		getCommandDefinition().getOptionDescription("bet"), true);
	betAmount.setMinValue(1);
	betAmount.setMaxValue(getCommandConfig().getMaxGameBetAmount());
	commandData.addOptions(betAmount);
	return commandData;
    }

    @Override
    protected ReactionMenu onGameStart(SlashCommandEvent event, BlankUser user,
	    GameMetadata metadata) {
	int betAmount = (int) event.getOption("bet").getAsLong();

	RouletteMetadata roulette = RouletteMetadata
		.builder()
		.betAmount(betAmount)
		.round(1)
		.build();

	try {
	    metadata.setMetadata(roulette);
	} catch (JsonProcessingException e) {
	    throw new RuntimeException(e);
	}

	FormattingData reply = playGame(metadata);

	reply(event, reply);

	System.out.println("Is Game Finished: " + metadata.isGameFinished());
	if (!metadata.isGameFinished()) {
	    return createMenuEntry(metadata);
	} else {
	    return null;
	}
    }

    @Override
    protected ReactionMenu onGameContinue(BlankUser user, GameMetadata metadata,
	    Object argument, Consumer<FormattingData> messageEdit) {
	if (argument instanceof List) {
	    messageEdit.accept(rejectNewCommand(user));
	    return null;
	}

	if (argument.equals("STOP")) {
	    finish(metadata);
	    return null;
	}

	FormattingData reply = playGame(metadata);

	messageEdit.accept(reply);
	if (!metadata.isGameFinished()) {
	    return createMenuEntry(metadata);
	} else {
	    return null;
	}
    }

    private FormattingData rejectNewCommand(BlankUser user) {
	return getBlankUserService()
		.createFormattingData(user,
			GameMessageType.ROULETTE_GAME_RUNNING)
		.build();
    }

    private ReactionMenu createMenuEntry(GameMetadata metadata) {
	ReactionMenu menu = new ReactionMenu(Duration.ofMinutes(1))
		.singleUse(true)
		.restricted(true)
		.allowedDiscordIds(List.of(metadata.getUser().getDiscordId()))
		.timeoutTask(() -> finish(metadata.getId()));

	createMenuEntry(menu, "🛑", "STOP");
	createMenuEntry(menu, "▶️", "CONTINUE");
	return menu;
    }

    private FormattingData playGame(GameMetadata metadata) {
	RouletteMetadata roulette;
	try {
	    roulette = metadata.getMetadata(RouletteMetadata.class);
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	BlankUser user = metadata.getUser();

	int betAmount = roulette.getBetAmount();

	if (user.getBalance() < betAmount) {
	    if (roulette.getRound() == 1) {
		abort(metadata);
	    } else {
		finish(metadata);
	    }
	    return getBlankUserService()
		    .createFormattingData(user,
			    GameMessageType.GAME_BET_NOT_ENOUGH_MONEY)
		    .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
		    .dataPairing(GameFormatDataKey.GAME_NAME,
			    getGameType().getDisplayName())
		    .build();
	}
	getBlankUserService().decreaseUserBalance(user, betAmount);

	int randomNumber = new Random().nextInt(6);
	boolean success = randomNumber != 0;
	System.out.println("Random Number: " + randomNumber);

	int reward = calculateReturn(betAmount);

	if (success) {
	    getBlankUserService().increaseUserBalance(user, reward);
	    roulette.setBetAmount(reward);
	    roulette.setRound(roulette.getRound() + 1);

	    return buildRouletteMessage(metadata, roulette, user, betAmount,
		    reward, GameMessageType.ROULETTE_WIN_MESSAGE);
	} else {
	    finish(metadata);
	    return buildRouletteMessage(metadata, roulette, user, betAmount,
		    reward, GameMessageType.ROULETTE_LOSE_MESSAGE);
	}
    }

    private FormattingData buildRouletteMessage(GameMetadata metadata,
	    RouletteMetadata roulette, BlankUser user, int betAmount,
	    int reward, MessageType resultMessage) {
	List<Integer> previousBetAmounts = new ArrayList<>();
	if (roulette.getPreviousBetAmounts() != null) {
	    previousBetAmounts = roulette.getPreviousBetAmounts();
	}
	previousBetAmounts.add(betAmount);

	String rouletteHeader = previousBetAmounts
		.stream()
		.map((bet) -> format(getBlankUserService()
			.createFormattingData(user,
				GameMessageType.ROULETTE_BET_AND_PULL)
			.dataPairing(GameFormatDataKey.BET_AMOUNT, bet)
			.build()))
		.collect(Collectors.joining("\n"));

	roulette.setPreviousBetAmounts(previousBetAmounts);

	try {
	    metadata.setMetadata(roulette);
	} catch (JsonProcessingException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}

	String rouletteResult = format(getBlankUserService()
		.createFormattingData(user, resultMessage)
		.dataPairing(GameFormatDataKey.REWARD_AMOUNT, reward)
		.dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
		.build());

	return getBlankUserService()
		.createFormattingData(user, GameMessageType.ROULETTE_DISPLAY)
		.dataPairing(GameFormatDataKey.ROULETTE_HEADER, rouletteHeader)
		.dataPairing(GameFormatDataKey.ROULETTE_RESULT, rouletteResult)
		.build();
    }

    private int calculateReturn(int betAmount) {
	return (int) Math.round((6d / 5d) * betAmount);
    }

}
