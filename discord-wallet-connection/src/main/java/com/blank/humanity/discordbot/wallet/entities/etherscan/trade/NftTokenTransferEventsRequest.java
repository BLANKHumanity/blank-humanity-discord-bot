package com.blank.humanity.discordbot.wallet.entities.etherscan.trade;

import java.util.Optional;

import javax.validation.constraints.Min;

import com.blank.humanity.discordbot.wallet.entities.etherscan.EtherscanApiAction;
import com.blank.humanity.discordbot.wallet.entities.etherscan.EtherscanApiModule;
import com.blank.humanity.discordbot.wallet.entities.etherscan.EtherscanRequest;
import com.blank.humanity.discordbot.wallet.entities.etherscan.EtherscanSort;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true, chain = true)
public class NftTokenTransferEventsRequest extends EtherscanRequest {

    public NftTokenTransferEventsRequest() {
	argument("contractaddress", this::contractaddress);
	argument("address", this::address);
	argument("page", this::page);
	argument("offset", this::offset);
	argument("startblock", this::startblock);
	argument("endblock", this::endblock);
	argument("sort",
		() -> Optional
			.ofNullable(this.sort())
			.map(EtherscanSort::getValue)
			.orElse(null));

	module(EtherscanApiModule.ACCOUNT);
	action(EtherscanApiAction.ERC721_TOKEN_TRANSFERS);
    }

    private String contractaddress;

    private String address;

    @Min(1)
    private Integer page;

    private Integer offset;

    private Long startblock;

    private Long endblock;

    private EtherscanSort sort;

}
