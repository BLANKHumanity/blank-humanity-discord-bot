package com.blank.humanity.discordbot.commands.games.messages;

import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum GameFormatDataKey implements FormatDataKey {
    GAME_NAME("gameName"), BET_AMOUNT("betAmount"), RPS_USER("rpsUser"),
    RPS_BOT("rpsBot"), ROULETTE_HEADER("rouletteHeader"),
    ROULETTE_RESULT("rouletteResult"), DICE_ROLL_USER("diceRollUser"),
    DICE_ROLL_OPPONENT("diceRollOpponent"), REWARD_AMOUNT("rewardAmount"),
    COOLDOWN_MINUTES("cooldownMinutes"), COOLDOWN_SECONDS("cooldownSeconds");

    @NonNull
    private String key;

    private boolean required = false;

}
