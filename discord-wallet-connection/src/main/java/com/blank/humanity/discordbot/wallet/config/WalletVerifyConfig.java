package com.blank.humanity.discordbot.wallet.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "web3.wallet.verify")
public class WalletVerifyConfig {

    @NotNull
    private String baseUrl;
    
}
