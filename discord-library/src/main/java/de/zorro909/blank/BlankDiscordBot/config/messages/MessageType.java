package de.zorro909.blank.BlankDiscordBot.config.messages;

import org.springframework.core.env.Environment;

import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;

public interface MessageType {
    
    public FormatDataKey[] getAvailableDataKeys();

    public String getMessageFormat(Environment env);

}
