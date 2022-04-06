package com.blank.humanity.discordbot.wallet.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "web3.nfts")
public class NftResolverConfig {

    private List<String> contracts;
    
}
