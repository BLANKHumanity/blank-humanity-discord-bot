package com.blank.humanity.discordbot.commands.items.messages;

import java.util.Optional;

import org.springframework.core.env.Environment;

import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ItemMessageType implements MessageType {
    SHOP_COMMAND_WRONG_PAGE, SHOP_TITLE_MESSAGE(ItemFormatDataKey.SHOP_PAGE),
    SHOP_HEADER(ItemFormatDataKey.SHOP_PAGE),
    SHOP_FOOTER(ItemFormatDataKey.SHOP_PAGE),
    SHOP_ITEM_DESCRIPTION(ItemFormatDataKey.SHOP_ITEM_ID,
	    ItemFormatDataKey.SHOP_ITEM_BUY_NAME,
	    ItemFormatDataKey.SHOP_ITEM_PRICE,
	    ItemFormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT),
    ITEM_USE_ACTION_UNDEFINED(ItemFormatDataKey.ITEM_ID,
	    ItemFormatDataKey.ITEM_NAME),
    ITEM_NOT_EXISTS(ItemFormatDataKey.ITEM_NAME),
    ITEM_USE_NOT_OWNED(ItemFormatDataKey.ITEM_ID, ItemFormatDataKey.ITEM_NAME),
    BUY_ITEM_NO_SUPPLY(ItemFormatDataKey.SHOP_ITEM_ID,
	    ItemFormatDataKey.ITEM_NAME, ItemFormatDataKey.SHOP_ITEM_BUY_NAME),
    BUY_ITEM_NOT_ENOUGH_MONEY(EconomyFormatDataKey.BALANCE,
	    ItemFormatDataKey.SHOP_ITEM_ID, ItemFormatDataKey.ITEM_NAME,
	    ItemFormatDataKey.SHOP_ITEM_BUY_NAME,
	    ItemFormatDataKey.SHOP_ITEM_PRICE),
    BUY_ITEM_SUCCESS(ItemFormatDataKey.SHOP_ITEM_ID,
	    ItemFormatDataKey.SHOP_ITEM_BUY_NAME, ItemFormatDataKey.ITEM_NAME,
	    ItemFormatDataKey.ITEM_AMOUNT),
    INVENTORY_ITEM_DESCRIPTION(ItemFormatDataKey.ITEM_ID,
	    ItemFormatDataKey.ITEM_AMOUNT, ItemFormatDataKey.ITEM_DESCRIPTION,
	    ItemFormatDataKey.ITEM_NAME),
    INVENTORY_ITEM_DESCRIPTION_WITH_USE(ItemFormatDataKey.ITEM_ID,
	    ItemFormatDataKey.ITEM_AMOUNT, ItemFormatDataKey.ITEM_DESCRIPTION,
	    ItemFormatDataKey.ITEM_NAME, ItemFormatDataKey.ITEM_USE_NAME),
    INVENTORY_DISPLAY(ItemFormatDataKey.INVENTORY_BODY),
    ITEM_GIVE_NOT_ENOUGH_OWNED(ItemFormatDataKey.ITEM_NAME,
	    ItemFormatDataKey.ITEM_AMOUNT, ItemFormatDataKey.ITEM_ID),
    ITEM_GIVE_SUCCESS(ItemFormatDataKey.ITEM_NAME, ItemFormatDataKey.ITEM_ID,
	    ItemFormatDataKey.ITEM_AMOUNT, GenericFormatDataKey.RECEIVING_USER,
	    GenericFormatDataKey.RECEIVING_USER_MENTION),
    ITEM_REMOVE_SUCCESS(ItemFormatDataKey.ITEM_NAME, ItemFormatDataKey.ITEM_ID,
	    ItemFormatDataKey.ITEM_AMOUNT, GenericFormatDataKey.RECEIVING_USER,
	    GenericFormatDataKey.RECEIVING_USER_MENTION);

    private ItemMessageType(FormatDataKey... keys) {
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
