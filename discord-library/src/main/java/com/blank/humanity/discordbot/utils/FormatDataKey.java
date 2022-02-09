package com.blank.humanity.discordbot.utils;

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
