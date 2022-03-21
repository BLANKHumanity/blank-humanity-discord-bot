package com.blank.humanity.discordbot.commands.games.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum GenericGameFormatDataKey implements FormatDataKey {
    GAME_NAME("gameName"), COOLDOWN_MINUTES("cooldownMinutes"),
    COOLDOWN_SECONDS("cooldownSeconds");

    @NonNull
    private String key;

    private boolean required = false;

}
