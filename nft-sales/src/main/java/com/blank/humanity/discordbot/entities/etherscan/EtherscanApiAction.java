package com.blank.humanity.discordbot.entities.etherscan;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EtherscanApiAction {

    ERC721_TOKEN_TRANSFERS("tokennfttx"), ETH_GET_TRANSACTION_BY_HASH("eth_getTransactionByHash"), GET_LOGS("getLogs");

    @JsonValue
    private String value;

}
