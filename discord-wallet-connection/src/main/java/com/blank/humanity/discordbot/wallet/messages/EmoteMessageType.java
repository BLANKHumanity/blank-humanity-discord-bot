package com.blank.humanity.discordbot.wallet.messages;

import java.util.Optional;

import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum EmoteMessageType implements MessageType {
    INITIALIZER_EMOTE_NFT_NOT_OWNED(EmoteFormatDataKey.NFT_ID),
    INITIALIZER_EMOTE_NOT_BOUGHT(EmoteFormatDataKey.EMOTE_ID),
    INITIALIZER_LEARN_UNKNOWN_EMOTE(EmoteFormatDataKey.EMOTE_ID),
    INITIALIZER_EMOTE_ALREADY_LEARNT(EmoteFormatDataKey.NFT_ID,
        EmoteFormatDataKey.EMOTE_ID),
    INITIALIZER_EMOTE_LEARN_NO_ITEM(EmoteFormatDataKey.NFT_ID,
        EmoteFormatDataKey.EMOTE_ID),
    INITIALIZER_LEARNS_EMOTE(EmoteFormatDataKey.NFT_ID,
        EmoteFormatDataKey.EMOTE_ID);

    private EmoteMessageType(FormatDataKey... keys) {
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
