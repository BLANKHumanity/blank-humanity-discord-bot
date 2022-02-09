package com.blank.humanity.discordbot.commands.utilities.messages;

import java.util.Optional;
import org.springframework.core.env.Environment;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum UtilityMessageType implements MessageType {
    CHAT_SUMMARY_LIST(UtilityFormatDataKey.PAGE, UtilityFormatDataKey.CHANNEL,
	    UtilityFormatDataKey.CHANNEL_MENTION,
	    UtilityFormatDataKey.PENDING_MARKER,
	    UtilityFormatDataKey.CHAT_SUMMARY_BODY),
    CHAT_SUMMARY_ENTRY(UtilityFormatDataKey.MESSAGE_COUNT),
    CHAT_SUMMARY_PENDING;

    private UtilityMessageType(FormatDataKey... keys) {
	this.availableDataKeys = keys;
    }

    private FormatDataKey[] availableDataKeys;

    public String getMessageFormat(Environment env) {
	return Optional
		.ofNullable(env.getProperty("messages." + name()))
		.orElseThrow(() -> new RuntimeException(
			"Non-existent Message Configuration '" + name()
				+ "'!"));
    }

}
