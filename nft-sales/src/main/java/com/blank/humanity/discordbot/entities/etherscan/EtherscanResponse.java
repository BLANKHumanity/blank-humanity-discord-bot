package com.blank.humanity.discordbot.entities.etherscan;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@ToString
@Jacksonized
public class EtherscanResponse {

    private String status;

    private String message;

}
