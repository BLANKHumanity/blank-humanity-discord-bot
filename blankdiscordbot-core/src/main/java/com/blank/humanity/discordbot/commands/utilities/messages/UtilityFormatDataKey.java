package com.blank.humanity.discordbot.commands.utilities.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum UtilityFormatDataKey implements FormatDataKey {
    PAGE("page"), CHANNEL("channel"), CHANNEL_MENTION("channelMention"),
    PENDING_MARKER("pendingMarker"), CHAT_SUMMARY_BODY("chatSummaryBody"),
    MESSAGE_COUNT("messageCount");

    @NonNull
    private String key;

    private boolean required = false;

}
