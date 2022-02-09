package com.blank.humanity.discordbot.commands.voting.messages;

import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum VotingFormatDataKey implements FormatDataKey {
    VOTE_CAMPAIGN_NAME("voteCampaignName"), RECEIVING_USER("receivingUser"),
    RECEIVING_USER_MENTION("receivingUserMention"), VOTE_CHOICE("voteChoice"),
    VOTE_COUNT("voteCount"),
    VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY("voteCampaignVoteChoiceDisplay"),
    VOTE_CAMPAIGN_LIST_BODY("voteCampaignListBody"),
    VOTE_CAMPAIGN_DESCRIPTION("voteCampaignDescription");

    @NonNull
    private String key;

    private boolean required = false;

}
