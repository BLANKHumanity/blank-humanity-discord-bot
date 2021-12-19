package de.zorro909.blank.BlankDiscordBot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum FormatDataKey {

    USER("user", true), USER_MENTION("userMention", true), BALANCE("balance"),
    CLAIM_STREAK("claimStreak"), CLAIM_REWARD("claimReward"), CLAIM_HOURS("claimHours"),
    CLAIM_MINUTES("claimMinutes"), CLAIM_SECONDS("claimSeconds");

    private FormatDataKey(String key) {
	this(key, false);
    }

    private final String key;

    private final boolean required;
}
