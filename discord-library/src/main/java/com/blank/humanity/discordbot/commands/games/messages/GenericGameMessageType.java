package com.blank.humanity.discordbot.commands.games.messages;

import java.util.Optional;

import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.config.messages.MessageType;

import lombok.Getter;

@Getter
public enum GenericGameMessageType implements MessageType {
    GAME_ON_COOLDOWN(GenericGameFormatDataKey.GAME_NAME,
	    GenericGameFormatDataKey.COOLDOWN_MINUTES,
	    GenericGameFormatDataKey.COOLDOWN_SECONDS);

    private GenericGameMessageType(GenericGameFormatDataKey... keys) {
	this.availableDataKeys = keys;
    }

    private GenericGameFormatDataKey[] availableDataKeys;

    public String getMessageFormat(Environment env) {
	return Optional
		.ofNullable(env.getProperty("messages." + name()))
		.orElseThrow(() -> new RuntimeException(
			"Non-existent Message Configuration '" + name()
				+ "'!"));
    }

}
