package com.blank.humanity.discordbot.itemActions.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum ItemActionFormatDataKey implements FormatDataKey {
    ROLE("role");

    @NonNull
    private String key;

    private boolean required = false;

}
