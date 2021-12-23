package de.zorro909.blank.BlankDiscordBot.config.messages;

import java.lang.reflect.Field;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.Getter;

@Getter
public enum MessageType {
    BALANCE_COMMAND_MESSAGE(FormatDataKey.BALANCE),
    DAILY_COMMAND_MESSAGE(FormatDataKey.CLAIM_REWARD),
    DAILY_COMMAND_MESSAGE_STREAK(FormatDataKey.CLAIM_REWARD,
	    FormatDataKey.CLAIM_STREAK),
    DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE(FormatDataKey.CLAIM_HOURS,
	    FormatDataKey.CLAIM_MINUTES, FormatDataKey.CLAIM_SECONDS),
    WORK_COMMAND_MESSAGE(FormatDataKey.CLAIM_REWARD),
    WORK_COMMAND_ALREADY_CLAIMED_MESSAGE(FormatDataKey.CLAIM_MINUTES,
	    FormatDataKey.CLAIM_SECONDS),
    SHOP_COMMAND_WRONG_PAGE, SHOP_TITLE_MESSAGE(FormatDataKey.SHOP_PAGE),
    SHOP_HEADER(FormatDataKey.SHOP_PAGE), SHOP_FOOTER(FormatDataKey.SHOP_PAGE),
    SHOP_ITEM_DESCRIPTION(FormatDataKey.SHOP_ITEM_ID,
	    FormatDataKey.SHOP_ITEM_BUY_NAME, FormatDataKey.SHOP_ITEM_PRICE,
	    FormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT),
    ITEM_USE_ACTION_UNDEFINED(FormatDataKey.ITEM_ID, FormatDataKey.ITEM_NAME),
    ITEM_NOT_EXISTS(FormatDataKey.ITEM_NAME),
    ITEM_USE_NOT_OWNED(FormatDataKey.ITEM_ID, FormatDataKey.ITEM_NAME),
    BUY_ITEM_NO_SUPPLY(FormatDataKey.SHOP_ITEM_ID, FormatDataKey.ITEM_NAME,
	    FormatDataKey.SHOP_ITEM_BUY_NAME),
    BUY_ITEM_NOT_ENOUGH_MONEY(FormatDataKey.BALANCE, FormatDataKey.SHOP_ITEM_ID,
	    FormatDataKey.ITEM_NAME, FormatDataKey.SHOP_ITEM_BUY_NAME,
	    FormatDataKey.SHOP_ITEM_PRICE),
    BUY_ITEM_SUCCESS(FormatDataKey.SHOP_ITEM_ID,
	    FormatDataKey.SHOP_ITEM_BUY_NAME, FormatDataKey.ITEM_NAME),
    INVENTORY_ITEM_DESCRIPTION(FormatDataKey.ITEM_ID, FormatDataKey.ITEM_AMOUNT,
	    FormatDataKey.ITEM_DESCRIPTION, FormatDataKey.ITEM_NAME),
    INVENTORY_ITEM_DESCRIPTION_WITH_USE(FormatDataKey.ITEM_ID,
	    FormatDataKey.ITEM_AMOUNT, FormatDataKey.ITEM_DESCRIPTION,
	    FormatDataKey.ITEM_NAME, FormatDataKey.ITEM_USE_NAME),
    INVENTORY_DISPLAY(FormatDataKey.INVENTORY_BODY),
    ERROR_MESSAGE(FormatDataKey.ERROR_MESSAGE),
    ROLE_REWARD_ALREADY_CLAIMED(FormatDataKey.ROLE),
    ROLE_REWARD_CLAIMED(FormatDataKey.ROLE), 
    RICHEST_COMMAND(FormatDataKey.RICHEST_LIST_PAGE, FormatDataKey.RICHEST_COMMAND_BODY),
    RICHEST_COMMAND_ENTRY(FormatDataKey.BALANCE, FormatDataKey.LEADERBOARD_PLACE);

    private MessageType(FormatDataKey... dataKeys) {
	this.availableDataKeys = dataKeys;
	try {
	    this.messageField = MessagesConfig.class.getDeclaredField(name());
	} catch (NoSuchFieldException | SecurityException e) {
	    e.printStackTrace();
	    System.exit(0);
	}
    }

    private Field messageField;

    private FormatDataKey[] availableDataKeys;

    public String getMessageFormat(MessagesConfig config)
	    throws IllegalArgumentException, IllegalAccessException {
	return (String) messageField.get(config);
    }

}
