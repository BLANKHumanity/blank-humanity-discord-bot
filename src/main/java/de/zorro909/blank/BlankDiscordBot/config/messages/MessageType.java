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
    DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE(FormatDataKey.COOLDOWN_HOURS,
	    FormatDataKey.COOLDOWN_MINUTES, FormatDataKey.COOLDOWN_SECONDS),
    WORK_COMMAND_MESSAGE(FormatDataKey.CLAIM_REWARD),
    WORK_COMMAND_ALREADY_CLAIMED_MESSAGE(FormatDataKey.COOLDOWN_MINUTES,
	    FormatDataKey.COOLDOWN_SECONDS),
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
    RICHEST_COMMAND_ENTRY(FormatDataKey.BALANCE, FormatDataKey.LEADERBOARD_PLACE), 
    VOTE_CAMPAIGN_EXISTS_ALREADY(FormatDataKey.VOTE_CAMPAIGN_NAME), 
    VOTE_CAMPAIGN_CREATED(FormatDataKey.VOTE_CAMPAIGN_NAME), 
    ITEM_GIVE_NOT_ENOUGH_OWNED(FormatDataKey.ITEM_NAME, FormatDataKey.ITEM_AMOUNT, FormatDataKey.ITEM_ID), 
    ITEM_GIVE_SUCCESS(FormatDataKey.ITEM_NAME, FormatDataKey.ITEM_ID, FormatDataKey.ITEM_AMOUNT, FormatDataKey.RECEIVING_USER, FormatDataKey.RECEIVING_USER_MENTION), 
    VOTE_CAMPAIGN_CHOICE_ADDED(FormatDataKey.VOTE_CAMPAIGN_NAME, FormatDataKey.VOTE_CHOICE),
    VOTE_CAMPAIGN_NOT_FOUND(FormatDataKey.VOTE_CAMPAIGN_NAME), 
    VOTE_CAMPAIGN_STARTED(FormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_STOPPED(FormatDataKey.VOTE_CAMPAIGN_NAME), 
    VOTE_CAMPAIGN_CHOICE_REMOVED(FormatDataKey.VOTE_CAMPAIGN_NAME, FormatDataKey.VOTE_CHOICE), 
    VOTE_CAMPAIGN_CHOICE_NOT_FOUND(FormatDataKey.VOTE_CAMPAIGN_NAME, FormatDataKey.VOTE_CHOICE), 
    VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY(FormatDataKey.VOTE_CHOICE, FormatDataKey.VOTE_COUNT),
    VOTE_CAMPAIGN_VOTE_DISPLAY_HEADER(FormatDataKey.VOTE_CAMPAIGN_NAME, FormatDataKey.VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY), 
    VOTE_COMMAND_ALREADY_VOTED(FormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_COMMAND_VOTED(FormatDataKey.VOTE_CAMPAIGN_NAME, FormatDataKey.VOTE_CHOICE),
    VOTE_CAMPAIGN_LIST_DESCRIPTION(FormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_LIST(FormatDataKey.VOTE_CAMPAIGN_LIST_BODY), 
    GAME_ON_COOLDOWN(FormatDataKey.GAME_NAME, FormatDataKey.COOLDOWN_MINUTES, FormatDataKey.COOLDOWN_SECONDS), 
    ROCK_PAPER_SCISSORS_TIE(FormatDataKey.BET_AMOUNT, FormatDataKey.RPS_USER, FormatDataKey.RPS_BOT), 
    ROCK_PAPER_SCISSORS_LOSS(FormatDataKey.BET_AMOUNT, FormatDataKey.RPS_USER, FormatDataKey.RPS_BOT),
    ROCK_PAPER_SCISSORS_WIN(FormatDataKey.BET_AMOUNT, FormatDataKey.RPS_USER, FormatDataKey.RPS_BOT), 
    GAME_BET_NOT_ENOUGH_MONEY(FormatDataKey.BET_AMOUNT), 
    ROULETTE_GAME_RUNNING(),
    ROULETTE_BET_AND_PULL(FormatDataKey.BET_AMOUNT),
    ROULETTE_WIN_MESSAGE(FormatDataKey.REWARD_AMOUNT),
    ROULETTE_LOSE_MESSAGE(FormatDataKey.REWARD_AMOUNT),
    ROULETTE_DISPLAY(FormatDataKey.ROULETTE_HEADER, FormatDataKey.ROULETTE_RESULT),
    DICE_GAME_LOSS(FormatDataKey.BET_AMOUNT, FormatDataKey.DICE_ROLL_USER, FormatDataKey.DICE_ROLL_OPPONENT),
    DICE_GAME_WIN(FormatDataKey.BET_AMOUNT, FormatDataKey.REWARD_AMOUNT, FormatDataKey.DICE_ROLL_USER, FormatDataKey.DICE_ROLL_OPPONENT),
    DICE_GAME_JACKPOT(FormatDataKey.BET_AMOUNT, FormatDataKey.REWARD_AMOUNT, FormatDataKey.DICE_ROLL_USER, FormatDataKey.DICE_ROLL_OPPONENT), 
    ITEM_USE_ONLY_SINGLE_ITEM(FormatDataKey.ITEM_ID, FormatDataKey.ITEM_NAME), 
    GIVE_COINS_COMMAND(FormatDataKey.REWARD_AMOUNT),
    CHAT_SUMMARY_LIST(FormatDataKey.PAGE, FormatDataKey.CHANNEL, FormatDataKey.CHANNEL_MENTION, FormatDataKey.PENDING_MARKER, FormatDataKey.CHAT_SUMMARY_BODY),
    CHAT_SUMMARY_ENTRY(FormatDataKey.MESSAGE_COUNT), 
    CHAT_SUMMARY_PENDING;

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
