package de.zorro909.blank.BlankDiscordBot.commands.items.messages;

import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum ItemFormatDataKey implements FormatDataKey {
    SHOP_PAGE("shopPage"), ITEM_ID("itemId"), ITEM_NAME("itemName"),
    SHOP_ITEM_ID("shopItemId"), SHOP_ITEM_PRICE("shopItemPrice"),
    SHOP_ITEM_BUY_NAME("shopItemBuyName"), ITEM_DESCRIPTION("itemDescription"),
    SHOP_ITEM_AVAILABLE_AMOUNT("shopItemAvailableAmount"),
    ITEM_USE_NAME("itemUseName"), ITEM_AMOUNT("itemAmount"),
    INVENTORY_BODY("inventoryBody");

    @NonNull
    private String key;

    private boolean required = false;

}
