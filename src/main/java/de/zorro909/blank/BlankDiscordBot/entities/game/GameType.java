package de.zorro909.blank.BlankDiscordBot.entities.game;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameType {
    ROCK_PAPER_SCISSORS("rps", "Rock paper scissors", 1, ChronoUnit.HOURS,
	    null),
    ROULETTE("roulette", "Roulette", 1, ChronoUnit.HOURS,
	    RouletteMetadata.class),
    DICE("dice", "Dice", 1, ChronoUnit.HOURS, null);

    private String commandName;
    private String displayName;
    private long cooldownAmount;
    private TemporalUnit cooldownTimeUnit;
    private Class<?> metadataClass;

    public boolean hasMetadataClass() {
	return metadataClass != null;
    }

}
