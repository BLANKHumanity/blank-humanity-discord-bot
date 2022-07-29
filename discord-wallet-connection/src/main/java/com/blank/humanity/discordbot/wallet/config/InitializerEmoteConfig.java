package com.blank.humanity.discordbot.wallet.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "emotes")
public class InitializerEmoteConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * Url to Emote API Endpoint
     */
    private String url;

    /**
     * List of all freely available emotes for Initializers
     */
    private List<String> unlocked;

    /**
     * List of all emotes for Initializers that need to be bought
     */
    private List<String> locked;

    private Integer emotePrice;

    private String size;
}
