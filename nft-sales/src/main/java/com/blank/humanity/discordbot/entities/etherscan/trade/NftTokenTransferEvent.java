package com.blank.humanity.discordbot.entities.etherscan.trade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class NftTokenTransferEvent {

    private long blockNumber;

    private String hash;

    private String contractAddress;

    private String from;

    private String to;

    private long tokenID;

    private String tokenName;

}
