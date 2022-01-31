package de.zorro909.blank.BlankDiscordBot.entities.etherscan;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EtherscanSort {
    ASCENDING("asc"), DESCENDING("desc");

    @JsonValue
    @Getter
    private String value;
}