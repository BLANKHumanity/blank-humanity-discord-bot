package com.blank.humanity.discordbot.config.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CustomFormatDataKey implements FormatDataKey {

    private final String key;

    @Builder.Default
    private boolean isRequired = false;

    public static CustomFormatDataKey key(String key) {
        return builder().key(key).build();
    }

}
