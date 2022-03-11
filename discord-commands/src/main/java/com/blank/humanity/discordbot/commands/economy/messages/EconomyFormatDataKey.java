package com.blank.humanity.discordbot.commands.economy.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum EconomyFormatDataKey implements FormatDataKey {
    BALANCE("balance"), CLAIM_STREAK("claimStreak"),
    CLAIM_REWARD("claimReward"), COOLDOWN_HOURS("cooldownHours"),
    COOLDOWN_MINUTES("cooldownMinutes"), COOLDOWN_SECONDS("cooldownSeconds"),
    RICHEST_LIST_PAGE("richestListPage"),
    RICHEST_COMMAND_BODY("richestCommandBody"),
    LEADERBOARD_PLACE("leaderboardPlace"), REWARD_AMOUNT("rewardAmount");

    @NonNull
    private String key;

    private boolean required = false;

}
