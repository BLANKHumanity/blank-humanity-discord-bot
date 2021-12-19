package de.zorro909.blank.BlankDiscordBot.config;

import javax.validation.Valid;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties("messages")
public class MessagesConfig {

    @NotNull
    @Value("%(user) has %(balance)")
    public String BALANCE_COMMAND_MESSAGE;

    @NotNull
    @Value("%(userMention) got %(claimReward) as a daily Reward!")
    public String DAILY_COMMAND_MESSAGE;

    @NotNull
    @Value("%(userMention) you can't claim more than once per day. You need to wait %(claimHours):%(claimMinutes):%(claimSeconds) to claim again.")
    public String DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE;
    
    @NotNull
    @Value("%(userMention) worked and got %(claimReward) as Reward!")
    public String WORK_COMMAND_MESSAGE;
    
    @NotNull
    @Value("%(userMention) you can't work more than once per hour. You need to wait %(claimMinutes) minutes %(claimSeconds) seconds to work again.")
    public String WORK_COMMAND_ALREADY_CLAIMED_MESSAGE;


}
