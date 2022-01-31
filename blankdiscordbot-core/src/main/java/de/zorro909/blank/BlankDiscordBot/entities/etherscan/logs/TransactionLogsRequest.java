package de.zorro909.blank.BlankDiscordBot.entities.etherscan.logs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.EtherscanApiAction;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.EtherscanApiModule;
import de.zorro909.blank.BlankDiscordBot.entities.etherscan.EtherscanRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true, fluent = true)
public class TransactionLogsRequest extends EtherscanRequest {

    public TransactionLogsRequest() {
	module(EtherscanApiModule.LOGS);
	action(EtherscanApiAction.GET_LOGS);

	argument("fromBlock", this::fromBlock);
	argument("toBlock", this::toBlock);
	argument("address", this::address);
	argument("topic0", this::topic0);
	argument("topic1", this::topic1);
	argument("topic2", this::topic2);
	argument("topic3", this::topic3);
	argument("topic0_1_opr", this::topic0_1_opr);
	argument("topic1_2_opr", this::topic1_2_opr);
	argument("topic2_3_opr", this::topic2_3_opr);
	argument("topic0_2_opr", this::topic0_2_opr);
	argument("topic0_3_opr", this::topic0_3_opr);
	argument("topic1_3_opr", this::topic1_3_opr);
    }

    private Long fromBlock;

    private Long toBlock;

    private String address;

    private String topic0;

    private String topic1;

    private String topic2;

    private String topic3;

    private String topic0_1_opr;

    private String topic1_2_opr;

    private String topic2_3_opr;

    private String topic0_2_opr;

    private String topic0_3_opr;

    private String topic1_3_opr;

}
