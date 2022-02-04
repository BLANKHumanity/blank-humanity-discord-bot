package de.zorro909.blank.BlankDiscordBot.config.messages;

import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum GenericFormatDataKey implements FormatDataKey {
    ERROR_MESSAGE("errorMessage"), USER("user"), USER_MENTION("userMention"),
    RECEIVING_USER("receivingUser"),
    RECEIVING_USER_MENTION("receivingUserMention");

    @NonNull
    private String key;

    private boolean required = false;
}
