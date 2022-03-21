package com.blank.humanity.discordbot.entities.etherscan;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EtherscanApiModule {

    ACCOUNT("account"), PROXY("proxy"), LOGS("logs");
    
    @JsonValue
    private String value;
    
}
