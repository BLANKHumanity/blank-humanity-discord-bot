package de.zorro909.blank.BlankDiscordBot.wallet.messages;

import java.util.Optional;
import org.springframework.core.env.Environment;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericFormatDataKey;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum WalletMessageType implements MessageType {
    WALLET_VERIFY_DISPLAY_LINK(WalletFormatDataKey.WALLET_VERIFY_LINK,
	    GenericFormatDataKey.USER, GenericFormatDataKey.USER_MENTION);

    private WalletMessageType(FormatDataKey... keys) {
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
