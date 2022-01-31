package de.zorro909.blank.BlankDiscordBot.entities.etherscan.logs;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionLogsResponse {

    private String status;

    private String message;

    private List<TransactionLogEntry> result;

}
