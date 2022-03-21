package com.blank.humanity.discordbot.config;

import java.security.SecureRandom;

import javax.security.auth.login.LoginException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

@Data
@NoArgsConstructor
@Validated
@Configuration
@ConfigurationProperties("discord")
public class DiscordBotConfig {

    @NonNull
    private String authToken;

    @Bean
    public static JDA jda(DiscordBotConfig discordBotConfig)
        throws LoginException, InterruptedException {
        return JDABuilder
            .createDefault(discordBotConfig.getAuthToken(),
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .build()
            .awaitReady();
    }

    @Bean
    public static SecureRandom random() {
        return new SecureRandom();
    }

}
