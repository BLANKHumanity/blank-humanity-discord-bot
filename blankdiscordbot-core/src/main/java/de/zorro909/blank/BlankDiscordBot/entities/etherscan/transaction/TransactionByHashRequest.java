package de.zorro909.blank.BlankDiscordBot.entities.etherscan.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.EtherscanApiAction;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.EtherscanApiModule;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.EtherscanRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true, chain = true)
public class TransactionByHashRequest extends EtherscanRequest {

    public TransactionByHashRequest() {
	argument("txhash", this::txhash, true);
	module(EtherscanApiModule.PROXY);
	action(EtherscanApiAction.ETH_GET_TRANSACTION_BY_HASH);
    }

    @NonNull
    private String txhash;

}
