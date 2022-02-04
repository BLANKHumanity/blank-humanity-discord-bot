package de.zorro909.blank.BlankDiscordBot.config.commands;

import java.util.HashMap;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import de.zorro909.blank.BlankDiscordBot.entities.user.ClaimDataType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Validated
@Configuration
@ConfigurationProperties("discord")
public class CommandConfig {

    @NotNull
    @Min(100000000000000000L)
    private long guildId;
    
    @NotNull
    @Min(0)
    private int minimumDailyReward;

    @NotNull
    @Min(0)
    private int maximumDailyReward;

    @NotNull
    @Min(0)
    private int minimumWorkReward;

    @NotNull
    @Min(0)
    private int maximumWorkReward;

    @NotNull
    @Min(1)
    private double rewardMultiplier;

    @NotNull
    @Min(1)
    private double streakMultiplier;

    @NotNull
    @Min(1)
    private int userListPageSize;

    @NotNull
    private HashMap<String, CommandDefinition> commandDefinitions = new HashMap<>();

    @NotNull
    private List<Long> hiddenCommandChannels = List.of();
    
    @NotNull
    private int maxGameBetAmount = 300;

    public CommandDefinition getCommandDefinition(String command) {
	return commandDefinitions
		.getOrDefault(command, new CommandDefinition());
    }

    public int getMinimumReward(ClaimDataType claimType) {
	return switch (claimType) {
	case DAILY_CLAIM -> minimumDailyReward;
	case WORK_CLAIM -> minimumWorkReward;
	};
    }

    public int getMaximumReward(@NotNull ClaimDataType claimType) {
	return switch (claimType) {
	case DAILY_CLAIM -> maximumDailyReward;
	case WORK_CLAIM -> maximumWorkReward;
	};
    }

}
