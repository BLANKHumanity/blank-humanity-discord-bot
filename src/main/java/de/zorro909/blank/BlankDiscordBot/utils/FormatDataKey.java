package de.zorro909.blank.BlankDiscordBot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum FormatDataKey {

    USER("user"), USER_MENTION("userMention"), BALANCE("balance"),
    CLAIM_STREAK("claimStreak"), CLAIM_REWARD("claimReward"),
    COOLDOWN_HOURS("cooldownHours"), COOLDOWN_MINUTES("cooldownMinutes"),
    COOLDOWN_SECONDS("cooldownSeconds"), SHOP_PAGE("shopPage"),
    ITEM_ID("itemId"), ITEM_NAME("itemName"), SHOP_ITEM_ID("shopItemId"),
    SHOP_ITEM_PRICE("shopItemPrice"), SHOP_ITEM_BUY_NAME("shopItemBuyName"),
    ITEM_DESCRIPTION("itemDescription"),
    SHOP_ITEM_AVAILABLE_AMOUNT("shopItemAvailableAmount"),
    ITEM_USE_NAME("itemUseName"), ITEM_AMOUNT("itemAmount"),
    INVENTORY_BODY("inventoryBody"), ERROR_MESSAGE("errorMessage"),
    ROLE("role"), RICHEST_LIST_PAGE("richestListPage"),
    RICHEST_COMMAND_BODY("richestCommandBody"),
    LEADERBOARD_PLACE("leaderboardPlace"),
    VOTE_CAMPAIGN_NAME("voteCampaignName"), RECEIVING_USER("receivingUser"),
    RECEIVING_USER_MENTION("receivingUserMention"), VOTE_CHOICE("voteChoice"),
    VOTE_COUNT("voteCount"),
    VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY("voteCampaignVoteChoiceDisplay"),
    VOTE_CAMPAIGN_LIST_BODY("voteCampaignListBody"),
    VOTE_CAMPAIGN_DESCRIPTION("voteCampaignDescription"), GAME_NAME("gameName"),
    BET_AMOUNT("betAmount"), RPS_USER("rpsUser"), RPS_BOT("rpsBot"),
    REWARD_AMOUNT("rewardAmount"), ROULETTE_HEADER("rouletteHeader"),
    ROULETTE_RESULT("rouletteResult"), DICE_ROLL_USER("diceRollUser"),
    DICE_ROLL_OPPONENT("diceRollOpponent");

    private FormatDataKey(String key) {
	this(key, false);
    }

    private final String key;

    private final boolean required;
}
