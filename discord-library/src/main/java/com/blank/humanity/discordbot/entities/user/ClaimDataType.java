package com.blank.humanity.discordbot.entities.user;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClaimDataType {
    DAILY_CLAIM(Duration.ofDays(1).toMillis(), true,
	    Duration.ofDays(2).toMillis()),
    WORK_CLAIM(Duration.ofHours(1).toMillis(), false, 0L);

    private Long millisBetweenClaims;
    private boolean streaksEnabled;
    private Long millisStreakDelay;
}
