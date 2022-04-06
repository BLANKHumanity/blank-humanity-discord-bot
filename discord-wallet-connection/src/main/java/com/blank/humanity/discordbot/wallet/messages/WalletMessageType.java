package com.blank.humanity.discordbot.wallet.messages;

import java.util.Optional;

import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum WalletMessageType implements MessageType {
    WALLET_VERIFY_DISPLAY_LINK(WalletFormatDataKey.WALLET_VERIFY_LINK,
        GenericFormatDataKey.USER, GenericFormatDataKey.USER_MENTION),
    SET_AIRDROP_WALLET_WRONG_FORMAT_ERROR(GenericFormatDataKey.USER,
        GenericFormatDataKey.USER_MENTION),
    SET_AIRDROP_WALLET_SUCCESS(WalletFormatDataKey.AIRDROP_WALLET,
        GenericFormatDataKey.USER, GenericFormatDataKey.USER_MENTION),
    INITIALIZER_EMOTE_NFT_NOT_OWNED(WalletFormatDataKey.NFT_ID),
    INITIALIZER_EMOTE_NOT_BOUGHT(WalletFormatDataKey.EMOTE_ID);

    private WalletMessageType(FormatDataKey... keys) {
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
