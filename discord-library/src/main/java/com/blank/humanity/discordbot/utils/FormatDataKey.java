package com.blank.humanity.discordbot.utils;

public interface FormatDataKey {
/*
    USER("user"), USER_MENTION("userMention"),
    ROLE("role"), 
    , , 
*/
    public String getKey();
    
    public boolean isRequired();
}
