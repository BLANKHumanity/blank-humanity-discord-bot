package com.blank.humanity.discordbot.config.messages;

import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.utils.FormatDataKey;

public interface MessageType {
    
    public FormatDataKey[] getAvailableDataKeys();

    public String getMessageFormat(Environment env);

}
