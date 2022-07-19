package com.blank.humanity.discordbot.wallet.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum EmoteFormatDataKey implements FormatDataKey {
    NFT_ID("nftId"), EMOTE_ID("emoteId");

    @NonNull
    private String key;

    private boolean required = false;

}
