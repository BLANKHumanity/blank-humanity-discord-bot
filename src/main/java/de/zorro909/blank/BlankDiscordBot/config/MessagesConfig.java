package de.zorro909.blank.BlankDiscordBot.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties("messages")
public class MessagesConfig {

    @NotNull
    @Value("%(user) has %(balance)")
    public String BALANCE_COMMAND_MESSAGE;

    @NotNull
    @Value("%(userMention) got %(claimReward) as a daily Reward!")
    public String DAILY_COMMAND_MESSAGE;

    @NotNull
    @Value("%(userMention) you can't claim more than once per day. You need to wait %(claimHours):%(claimMinutes):%(claimSeconds) to claim again.")
    public String DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE;

    @NotNull
    @Value("%(userMention) worked and got %(claimReward) as Reward!")
    public String WORK_COMMAND_MESSAGE;

    @NotNull
    @Value("%(userMention) you can't work more than once per hour. You need to wait %(claimMinutes) minutes %(claimSeconds) seconds to work again.")
    public String WORK_COMMAND_ALREADY_CLAIMED_MESSAGE;

    @NotNull
    @Value("%(userMention) you tried to access a non existing Shop Page!")
    public String SHOP_COMMAND_WRONG_PAGE;

    @NotNull
    @Value("Shop Page %(shopPage)")
    public String SHOP_TITLE_MESSAGE;

    @NotNull
    @Value("Use /buy <item name> to buy an Item")
    public String SHOP_HEADER;

    @NotNull
    @Value("")
    public String SHOP_FOOTER;

    @NotNull
    @Value("%(shopItemId) - %(itemName) - %(shopItemPrice) - Available: %(shopItemAvailableAmount)\n----%(itemDescription) : /buy %(shopItemBuyName)")
    public String SHOP_ITEM_DESCRIPTION;

    @NotNull
    @Value("%(userMention), this Item has no usage Action defined. This is likely a configuration error. Please contact an administrator!")
    public String ITEM_USE_ACTION_UNDEFINED;

    @NotNull
    @Value("%(userMention), the Item '%(itemName)' does not exist")
    public String ITEM_NOT_EXISTS;

    @NotNull
    @Value("%(userMention), you don't have this item")
    public String ITEM_USE_NOT_OWNED;

    @NotNull
    @Value("%(userMention), this Item has no Supply left")
    public String BUY_ITEM_NO_SUPPLY;

    @NotNull
    @Value("%(userMention), you need to have %(shopItemPrice), but you only have %(balance)")
    public String BUY_ITEM_NOT_ENOUGH_MONEY;

    @NotNull
    @Value("%(userMention), you bought the Item '%(itemName)")
    public String BUY_ITEM_SUCCESS;

    @NotNull
    @Value("%(itemAmount)x %(itemName)")
    public String INVENTORY_ITEM_DESCRIPTION;
    
    @NotNull
    @Value("%(itemAmount)x %(itemName) - /use %(itemUseName)")
    public String INVENTORY_ITEM_DESCRIPTION_WITH_USE;

}
