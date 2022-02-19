package com.blank.humanity.discordbot.commands.games.messages;

import java.util.Optional;
import org.springframework.core.env.Environment;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.utils.FormatDataKey;
import lombok.Getter;

@Getter
public enum GameMessageType implements MessageType {
    GAME_ON_COOLDOWN(GenericGameFormatDataKey.GAME_NAME,
	    GenericGameFormatDataKey.COOLDOWN_MINUTES,
	    GenericGameFormatDataKey.COOLDOWN_SECONDS),
    ROCK_PAPER_SCISSORS_TIE(GameFormatDataKey.BET_AMOUNT,
	    GameFormatDataKey.RPS_USER, GameFormatDataKey.RPS_BOT),
    ROCK_PAPER_SCISSORS_LOSS(GameFormatDataKey.BET_AMOUNT,
	    GameFormatDataKey.RPS_USER, GameFormatDataKey.RPS_BOT),
    ROCK_PAPER_SCISSORS_WIN(GameFormatDataKey.BET_AMOUNT, GameFormatDataKey.REWARD_AMOUNT,
	    GameFormatDataKey.RPS_USER, GameFormatDataKey.RPS_BOT),
    GAME_BET_NOT_ENOUGH_MONEY(GameFormatDataKey.BET_AMOUNT),
    ROULETTE_GAME_RUNNING(),
    ROULETTE_BET_AND_PULL(GameFormatDataKey.BET_AMOUNT),
    ROULETTE_WIN_MESSAGE(GameFormatDataKey.REWARD_AMOUNT),
    ROULETTE_LOSE_MESSAGE(GameFormatDataKey.REWARD_AMOUNT),
    ROULETTE_DISPLAY(GameFormatDataKey.ROULETTE_HEADER,
	    GameFormatDataKey.ROULETTE_RESULT),
    DICE_GAME_LOSS(GameFormatDataKey.BET_AMOUNT,
	    GameFormatDataKey.DICE_ROLL_USER,
	    GameFormatDataKey.DICE_ROLL_OPPONENT),
    DICE_GAME_WIN(GameFormatDataKey.BET_AMOUNT, GameFormatDataKey.REWARD_AMOUNT,
	    GameFormatDataKey.DICE_ROLL_USER,
	    GameFormatDataKey.DICE_ROLL_OPPONENT),
    DICE_GAME_JACKPOT(GameFormatDataKey.BET_AMOUNT,
	    GameFormatDataKey.REWARD_AMOUNT, GameFormatDataKey.DICE_ROLL_USER,
	    GameFormatDataKey.DICE_ROLL_OPPONENT),
	    DICE_GAME_DRAW(GameFormatDataKey.BET_AMOUNT,
	        GameFormatDataKey.DICE_ROLL_USER,
	        GameFormatDataKey.DICE_ROLL_OPPONENT);

    private GameMessageType(FormatDataKey... keys) {
	this.availableDataKeys = keys;
    }

    private FormatDataKey[] availableDataKeys;

    public String getMessageFormat(Environment env) {
	return Optional
		.ofNullable(env.getProperty("messages." + name()))
		.orElseThrow(() -> new RuntimeException(
			"Non-existent Message Configuration '" + name()
				+ "'!"));
    }

}
