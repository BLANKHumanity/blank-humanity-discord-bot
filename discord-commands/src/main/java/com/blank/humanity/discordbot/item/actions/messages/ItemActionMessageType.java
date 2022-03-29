package com.blank.humanity.discordbot.item.actions.messages;

import java.util.Optional;

import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ItemActionMessageType implements MessageType {
    ROLE_REWARD_ALREADY_CLAIMED(ItemActionFormatDataKey.ROLE),
    ROLE_REWARD_CLAIMED(ItemActionFormatDataKey.ROLE),
    ITEM_USE_ONLY_SINGLE_ITEM(ItemFormatDataKey.ITEM_ID,
        ItemFormatDataKey.ITEM_NAME),
    ITEM_USE_ONLY_REQUIRED_AMOUNT(ItemFormatDataKey.ITEM_ID,
        ItemFormatDataKey.ITEM_NAME, ItemFormatDataKey.ITEM_AMOUNT),
    ITEM_USE_TOO_MANY_REPLIES(ItemFormatDataKey.ITEM_ID,
        ItemFormatDataKey.ITEM_NAME);

    private ItemActionMessageType(FormatDataKey... keys) {
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
