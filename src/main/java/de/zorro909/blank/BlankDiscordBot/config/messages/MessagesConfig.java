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

    @NotNull
    public String VOTE_CAMPAIGN_CREATED;

    @NotNull
    public String VOTE_CAMPAIGN_EXISTS_ALREADY;

    @NotNull
    public String VOTE_CAMPAIGN_CHOICE_ADDED;

    @NotNull
    public String VOTE_CAMPAIGN_NOT_FOUND;

    @NotNull
    public String VOTE_CAMPAIGN_STARTED;
    
    @NotNull
    public String VOTE_CAMPAIGN_STOPPED;
    
    @NotNull
    public String VOTE_CAMPAIGN_CHOICE_REMOVED;
    
    @NotNull
    public String VOTE_CAMPAIGN_CHOICE_NOT_FOUND;
    
    @NotNull
    public String VOTE_CAMPAIGN_VOTE_DISPLAY_HEADER;
    
    @NotNull
    public String VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY;
    
    @NotNull
    public String VOTE_CAMPAIGN_LIST;
    
    @NotNull
    public String VOTE_CAMPAIGN_LIST_DESCRIPTION;
    
    @NotNull
    public String VOTE_COMMAND_ALREADY_VOTED;
    
    @NotNull
    public String VOTE_COMMAND_VOTED;
    
    @NotNull
    public String ITEM_GIVE_NOT_ENOUGH_OWNED;

    @NotNull
    public String ITEM_GIVE_SUCCESS;
    
    @NotNull
    public String GAME_ON_COOLDOWN;
    
    @NotNull
    public String ROCK_PAPER_SCISSORS_TIE;
    
    @NotNull
    public String ROCK_PAPER_SCISSORS_WIN;
    
    @NotNull
    public String ROCK_PAPER_SCISSORS_LOSS;
    
    @NotNull
    public String GAME_BET_NOT_ENOUGH_MONEY;
    
    @NotNull
    public String ROULETTE_GAME_RUNNING;
    
    @NotNull
    public String ROULETTE_BET_AND_PULL;
    
    @NotNull
    public String ROULETTE_WIN_MESSAGE;
    
    @NotNull
    public String ROULETTE_LOSE_MESSAGE;
    
    @NotNull
    public String ROULETTE_DISPLAY;
    
    @NotNull
    public String DICE_GAME_LOSS;
    
    @NotNull
    public String DICE_GAME_WIN;
    
    @NotNull
    public String DICE_GAME_JACKPOT;
    
    @NotNull
    public String ITEM_USE_ONLY_SINGLE_ITEM;

}
