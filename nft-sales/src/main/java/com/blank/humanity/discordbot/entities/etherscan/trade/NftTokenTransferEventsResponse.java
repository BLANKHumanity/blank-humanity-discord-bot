package com.blank.humanity.discordbot.entities.etherscan.trade;

import java.util.List;

import com.blank.humanity.discordbot.entities.etherscan.EtherscanResponse;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@ToString(callSuper = true)
@Jacksonized
public class NftTokenTransferEventsResponse extends EtherscanResponse {

    private List<NftTokenTransferEvent> result;

}
