package com.blank.humanity.discordbot.config.web3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix="web3.provider")
public class Web3jConfiguration {

    private String url;
    
}
