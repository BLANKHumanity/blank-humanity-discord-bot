package de.zorro909.blank.BlankDiscordBot.entities.etherscan.trade;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@ToString
@Jacksonized
public class NftTokenTransferEventsResponse {

    private String status;
    
    private String message;
    
    private List<NftTokenTransferEvent> result;
}
