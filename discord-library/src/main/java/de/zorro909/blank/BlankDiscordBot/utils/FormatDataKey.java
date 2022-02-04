package de.zorro909.blank.BlankDiscordBot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public interface FormatDataKey {
/*
    USER("user"), USER_MENTION("userMention"),
    ROLE("role"), 
    , , 
*/
    public String getKey();
    
    public boolean isRequired();
}
