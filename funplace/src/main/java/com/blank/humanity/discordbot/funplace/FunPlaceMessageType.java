package com.blank.humanity.discordbot.funplace;

import java.util.Optional;
import org.springframework.core.env.Environment;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.utils.FormatDataKey;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum FunPlaceMessageType implements MessageType {
    FUN_PLACE_SHOP_TITLE_MESSAGE(FunPlaceFormatDataKey.SHOP_PAGE),
    FUN_PLACE_SHOP_HEADER(FunPlaceFormatDataKey.SHOP_PAGE),
    FUN_PLACE_SHOP_FOOTER(FunPlaceFormatDataKey.SHOP_PAGE),
    SHOP_COMMAND_WRONG_PAGE,
    SHOP_ITEM_DESCRIPTION(FunPlaceFormatDataKey.SHOP_ITEM_ID,
	    FunPlaceFormatDataKey.SHOP_ITEM_BUY_NAME,
	    FunPlaceFormatDataKey.SHOP_ITEM_PRICE,
	    FunPlaceFormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT),
    ITEM_NOT_EXISTS(FunPlaceFormatDataKey.ITEM_NAME),
    BUY_ITEM_NO_SUPPLY(FunPlaceFormatDataKey.SHOP_ITEM_ID,
	    FunPlaceFormatDataKey.ITEM_NAME,
	    FunPlaceFormatDataKey.SHOP_ITEM_BUY_NAME),
    BUY_ITEM_NOT_ENOUGH_MONEY(FunPlaceFormatDataKey.SHOP_ITEM_ID,
	    FunPlaceFormatDataKey.ITEM_NAME,
	    FunPlaceFormatDataKey.SHOP_ITEM_BUY_NAME,
	    FunPlaceFormatDataKey.SHOP_ITEM_PRICE),
    BUY_ITEM_SUCCESS(FunPlaceFormatDataKey.SHOP_ITEM_ID,
	    FunPlaceFormatDataKey.SHOP_ITEM_BUY_NAME,
	    FunPlaceFormatDataKey.ITEM_NAME, FunPlaceFormatDataKey.ITEM_AMOUNT);

    private FunPlaceMessageType(FormatDataKey... keys) {
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
