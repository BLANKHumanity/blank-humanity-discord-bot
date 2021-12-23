package de.zorro909.blank.BlankDiscordBot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum FormatDataKey {

    USER("user", true), USER_MENTION("userMention", true), BALANCE("balance"),
    CLAIM_STREAK("claimStreak"), CLAIM_REWARD("claimReward"),
    CLAIM_HOURS("claimHours"), CLAIM_MINUTES("claimMinutes"),
    CLAIM_SECONDS("claimSeconds"), SHOP_PAGE("shopPage"), ITEM_ID("itemId"),
    ITEM_NAME("itemName"), SHOP_ITEM_ID("shopItemId"),
    SHOP_ITEM_PRICE("shopItemPrice"), SHOP_ITEM_BUY_NAME("shopItemBuyName"),
    ITEM_DESCRIPTION("itemDescription"),
    SHOP_ITEM_AVAILABLE_AMOUNT("shopItemAvailableAmount"),
    ITEM_USE_NAME("itemUseName"), ITEM_AMOUNT("itemAmount"),
    INVENTORY_BODY("inventoryBody"), ERROR_MESSAGE("errorMessage"),
    ROLE("role"), RICHEST_LIST_PAGE("richestListPage"),
    RICHEST_COMMAND_BODY("richestCommandBody"), LEADERBOARD_PLACE("leaderboardPlace");

    private FormatDataKey(String key) {
	this(key, false);
    }

    private final String key;

    private final boolean required;
}
