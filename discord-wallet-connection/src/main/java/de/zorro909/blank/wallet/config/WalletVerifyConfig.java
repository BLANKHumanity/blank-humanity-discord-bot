package de.zorro909.blank.wallet.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "web3.walletVerify")
public class WalletVerifyConfig {

    @NotNull
    private String verifyBaseUrl;
    
}
