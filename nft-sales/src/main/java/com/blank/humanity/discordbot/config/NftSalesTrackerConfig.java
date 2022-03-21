package com.blank.humanity.discordbot.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Validated
@Configuration
@ConfigurationProperties("web3.salestracker")
public class NftSalesTrackerConfig {
    
    private Long salesChannel;
    
    private Map<String, String> contractWatchList = Map.of();

}
