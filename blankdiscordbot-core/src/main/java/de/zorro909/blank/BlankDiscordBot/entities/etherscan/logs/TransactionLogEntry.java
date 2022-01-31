package de.zorro909.blank.BlankDiscordBot.entities.etherscan.logs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLogEntry {

    private String address;

    private List<String> topics;

    private String data;

    private String blockNumber;

    private String timeStamp;

    private String gasPrice;

    private String logIndex;

    private String transactionHash;

    private String transactionIndex;

    public String getDataPoint(int index) {
	return getData().substring(2).substring(index * 64, index * 64 + 64);
    }

}
