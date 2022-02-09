package com.blank.humanity.discordbot.itemActions.messages;

import java.util.Optional;
import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;

import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
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
	    ItemFormatDataKey.ITEM_NAME, ItemFormatDataKey.ITEM_AMOUNT);

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
