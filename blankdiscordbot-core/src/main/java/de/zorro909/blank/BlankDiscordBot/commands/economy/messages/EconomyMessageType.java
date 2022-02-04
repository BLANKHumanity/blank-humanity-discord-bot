package de.zorro909.blank.BlankDiscordBot.commands.economy.messages;

import java.util.Optional;
import org.springframework.core.env.Environment;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum EconomyMessageType implements MessageType {
    BALANCE_COMMAND_MESSAGE(EconomyFormatDataKey.BALANCE),
    DAILY_COMMAND_MESSAGE(EconomyFormatDataKey.CLAIM_REWARD),
    DAILY_COMMAND_MESSAGE_STREAK(EconomyFormatDataKey.CLAIM_REWARD,
	    EconomyFormatDataKey.CLAIM_STREAK),
    DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE(EconomyFormatDataKey.COOLDOWN_HOURS,
	    EconomyFormatDataKey.COOLDOWN_MINUTES,
	    EconomyFormatDataKey.COOLDOWN_SECONDS),
    WORK_COMMAND_MESSAGE(EconomyFormatDataKey.CLAIM_REWARD),
    WORK_COMMAND_ALREADY_CLAIMED_MESSAGE(EconomyFormatDataKey.COOLDOWN_MINUTES,
	    EconomyFormatDataKey.COOLDOWN_SECONDS),
    RICHEST_COMMAND(EconomyFormatDataKey.RICHEST_LIST_PAGE,
	    EconomyFormatDataKey.RICHEST_COMMAND_BODY),
    RICHEST_COMMAND_ENTRY(EconomyFormatDataKey.BALANCE,
	    EconomyFormatDataKey.LEADERBOARD_PLACE),
    GIVE_COINS_COMMAND(EconomyFormatDataKey.REWARD_AMOUNT);

    private EconomyMessageType(FormatDataKey... keys) {
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
