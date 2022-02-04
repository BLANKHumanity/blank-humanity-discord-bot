package de.zorro909.blank.BlankDiscordBot.config.messages;

import java.util.Optional;
import org.springframework.core.env.Environment;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.Getter;

@Getter
public enum GenericMessageType implements MessageType {
    ERROR_MESSAGE(GenericFormatDataKey.ERROR_MESSAGE);

    private GenericMessageType(FormatDataKey... keys) {
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
