package com.blank.humanity.discordbot.wallet.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "emotes")
public class InitializerEmoteConfig {

    /**
     * List of all emotes for Initializers
     */
    private List<String> initializers;

    private String size;
}
