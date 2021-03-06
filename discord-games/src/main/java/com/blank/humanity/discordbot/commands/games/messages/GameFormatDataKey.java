package com.blank.humanity.discordbot.commands.games.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum GameFormatDataKey implements FormatDataKey {
    BET_AMOUNT("betAmount"), RPS_USER("rpsUser"), RPS_BOT("rpsBot"),
    ROULETTE_HEADER("rouletteHeader"), ROULETTE_RESULT("rouletteResult"),
    DICE_ROLL_USER("diceRollUser"), DICE_ROLL_OPPONENT("diceRollOpponent"),
    REWARD_AMOUNT("rewardAmount");

    @NonNull
    private String key;

    private boolean required = false;

}
