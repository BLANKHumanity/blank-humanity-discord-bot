package com.blank.humanity.discordbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Validated
@Configuration
@ConfigurationProperties("web3.etherscan")
public class EtherscanApiConfig {

    private String apiKey;  
    
}
