package com.blank.humanity.discordbot.wallet.entities.etherscan;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@ToString
@Jacksonized
public class EtherscanResponse {

    protected static final ObjectMapper mapper = new ObjectMapper();

    private String status;

    private String message;

    private Object result;

}
