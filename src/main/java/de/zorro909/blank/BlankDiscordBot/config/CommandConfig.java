package de.zorro909.blank.BlankDiscordBot.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import de.zorro909.blank.BlankDiscordBot.entities.ClaimDataType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Validated
@Configuration
@ConfigurationProperties("discord")
public class CommandConfig {

    @NotNull
    @Min(0)
    @Value("25")
    private int minimumDailyReward;

    @NotNull
    @Min(0)
    @Value("100")
    private int maximumDailyReward;

    @NotNull
    @Min(0)
    @Value("25")
    private int minimumWorkReward;

    @NotNull
    @Min(0)
    @Value("100")
    private int maximumWorkReward;

    @NotNull
    @Min(1)
    @Value("1")
    private double rewardMultiplier;

    @NotNull
    @Min(1)
    @Value("1.1")
    private double streakMultiplier;
    
    @NotNull
    @Min(1)
    @Value("8")
    private int userListPageSize;

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
