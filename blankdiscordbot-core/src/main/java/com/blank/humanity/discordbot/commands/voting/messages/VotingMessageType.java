package com.blank.humanity.discordbot.commands.voting.messages;

import java.util.Optional;
import org.springframework.core.env.Environment;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum VotingMessageType implements MessageType {
    VOTE_CAMPAIGN_EXISTS_ALREADY(VotingFormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_CREATED(VotingFormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_CHOICE_ADDED(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
	    VotingFormatDataKey.VOTE_CHOICE),
    VOTE_CAMPAIGN_NOT_FOUND(VotingFormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_STARTED(VotingFormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_STOPPED(VotingFormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_CHOICE_REMOVED(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
	    VotingFormatDataKey.VOTE_CHOICE),
    VOTE_CAMPAIGN_CHOICE_NOT_FOUND(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
	    VotingFormatDataKey.VOTE_CHOICE),
    VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY(VotingFormatDataKey.VOTE_CHOICE,
	    VotingFormatDataKey.VOTE_COUNT),
    VOTE_CAMPAIGN_VOTE_DISPLAY_HEADER(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
	    VotingFormatDataKey.VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY),
    VOTE_COMMAND_ALREADY_VOTED(VotingFormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_COMMAND_VOTED(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
	    VotingFormatDataKey.VOTE_CHOICE),
    VOTE_CAMPAIGN_LIST_DESCRIPTION(VotingFormatDataKey.VOTE_CAMPAIGN_NAME),
    VOTE_CAMPAIGN_LIST(VotingFormatDataKey.VOTE_CAMPAIGN_LIST_BODY);

    private VotingMessageType(FormatDataKey... keys) {
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
