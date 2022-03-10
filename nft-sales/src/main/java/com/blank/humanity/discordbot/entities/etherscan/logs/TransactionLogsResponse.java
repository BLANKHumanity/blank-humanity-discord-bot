package com.blank.humanity.discordbot.entities.etherscan.logs;

import java.util.List;

import com.blank.humanity.discordbot.entities.etherscan.EtherscanResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@SuperBuilder
@ToString(callSuper = true)
public class TransactionLogsResponse extends EtherscanResponse {

    @JsonProperty(value = "result", required = true)
    private List<TransactionLogEntry> transactionLogs;

}
