package de.zorro909.blank.BlankDiscordBot.config.messages;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Setter;

@Validated
@Setter
@Configuration
@ConfigurationProperties(prefix = "messages")
public class MessagesConfig {

    @NotNull
    public String BALANCE_COMMAND_MESSAGE;

    @NotNull
    public String DAILY_COMMAND_MESSAGE;

    @NotNull
    public String DAILY_COMMAND_MESSAGE_STREAK;

    @NotNull
    public String DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE;

    @NotNull
    public String WORK_COMMAND_MESSAGE;

    @NotNull
    public String WORK_COMMAND_ALREADY_CLAIMED_MESSAGE;

    @NotNull
    public String SHOP_COMMAND_WRONG_PAGE;

    @NotNull
    public String SHOP_TITLE_MESSAGE;

    @NotNull
    public String SHOP_HEADER;

    @NotNull
    public String SHOP_FOOTER;

    @NotNull
    public String SHOP_ITEM_DESCRIPTION;

    @NotNull
    public String ITEM_USE_ACTION_UNDEFINED;

    @NotNull
    public String ITEM_NOT_EXISTS;

    @NotNull
    public String ITEM_USE_NOT_OWNED;

    @NotNull
    public String BUY_ITEM_NO_SUPPLY;

    @NotNull
    public String BUY_ITEM_NOT_ENOUGH_MONEY;

    @NotNull
    public String BUY_ITEM_SUCCESS;

    @NotNull
    public String INVENTORY_ITEM_DESCRIPTION;

    @NotNull
    public String INVENTORY_ITEM_DESCRIPTION_WITH_USE;

    @NotNull
    public String INVENTORY_DISPLAY;

    @NotNull
    public String ERROR_MESSAGE;

    @NotNull
    public String ROLE_REWARD_ALREADY_CLAIMED;

    @NotNull
    public String ROLE_REWARD_CLAIMED;
    
    @NotNull
    public String RICHEST_COMMAND;
    
    @NotNull
    public String RICHEST_COMMAND_ENTRY;
}
