package com.blank.humanity.discordbot.wallet.entities.etherscan.trade;

import java.util.List;

import com.blank.humanity.discordbot.wallet.entities.etherscan.EtherscanResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@ToString(callSuper = true)
@Jacksonized
public class NftTokenTransferEventsResponse extends EtherscanResponse {

    @JsonProperty(value = "result", required = true)
    private List<NftTokenTransferEvent> tokenTransferEvents;

}
