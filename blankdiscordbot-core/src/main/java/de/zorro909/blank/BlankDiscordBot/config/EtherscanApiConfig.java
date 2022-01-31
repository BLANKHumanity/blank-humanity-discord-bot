package de.zorro909.blank.BlankDiscordBot.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Validated
@Configuration
@ConfigurationProperties("etherscan")
public class EtherscanApiConfig {

    private String apiKey;  
    
}
