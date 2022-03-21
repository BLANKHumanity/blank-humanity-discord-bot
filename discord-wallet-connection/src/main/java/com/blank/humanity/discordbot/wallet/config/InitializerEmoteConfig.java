package com.blank.humanity.discordbot.wallet.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.blank.humanity.discordbot.wallet.entities.EmoteDefinition;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "emotes")
public class InitializerEmoteConfig {

    private Map<String, EmoteDefinition> initializers;
    
}
