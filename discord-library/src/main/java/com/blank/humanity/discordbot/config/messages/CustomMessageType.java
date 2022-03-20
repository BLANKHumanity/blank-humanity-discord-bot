package com.blank.humanity.discordbot.config.messages;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
public class CustomMessageType implements MessageType {

    @Singular(value = "key")
    private List<FormatDataKey> availableDataKeys;

    @NotNull
    private String format;

    public FormatDataKey[] getAvailableDataKeys() {
	return availableDataKeys.toArray(new FormatDataKey[] {});
    }

    @Override
    public String getMessageFormat(Environment env) {
	return format;
    }

}
