package com.blank.humanity.discordbot.itemActions;

import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.config.messages.CustomMessageType;
import com.blank.humanity.discordbot.itemActions.messages.ItemActionMessageType;

import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericFormatDataKey;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericMessageType;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.NamedFormatter;
import de.zorro909.blank.BlankDiscordBot.utils.item.ExecutableItemAction;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

@Component
public class MessageAction implements ExecutableItemAction {

    @Autowired
    private JDA jda;

    @Autowired
    private BlankUserService blankUserService;

    @Override
    public ItemActionStatus executeAction(BlankUser user, ItemDefinition item,
	    int amount, Consumer<FormattingData> reply) {
	Map<String, Object> args = item.getActionArguments();

	int requiredAmount = (int) args.getOrDefault("requiredAmount", 1);
	String replyMessage = (String) args.get("replyMessage");
	Long messageSendChannel = (Long) args.get("messageSendChannel");
	String messageSend = (String) args.get("messageSend");

	if (amount != requiredAmount) {
	    reply
		    .accept(blankUserService
			    .createFormattingData(user,
				    ItemActionMessageType.ITEM_USE_ONLY_REQUIRED_AMOUNT)
			    .dataPairing(ItemFormatDataKey.ITEM_ID,
				    item.getId())
			    .dataPairing(ItemFormatDataKey.ITEM_NAME,
				    item.getName())
			    .dataPairing(ItemFormatDataKey.ITEM_AMOUNT,
				    requiredAmount)
			    .build());
	    return ItemActionStatus.GENERIC_ERROR;
	}

	if (replyMessage == null) {
	    reply.accept(error(user, item.getId(), "replyMessage"));
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}

	if (messageSendChannel != null) {
	    TextChannel channel = jda.getTextChannelById(messageSendChannel);

	    if (channel == null) {
		reply.accept(error(user, item.getId(), "messageSendChannel"));
		return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	    }

	    if (messageSend == null) {
		reply.accept(error(user, item.getId(), "messageSend"));
		return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	    }

	    channel
		    .sendMessage(createMessage(messageSend, user, amount))
		    .queue();
	}

	reply.accept(createReply(replyMessage, user, amount));

	return ItemActionStatus.SUCCESS;
    }

    private String createMessage(String replyMessage, BlankUser user,
	    int amount) {
	FormattingData data = createReply(replyMessage, user, amount);
	return NamedFormatter.namedFormat(replyMessage, data.getDataPairings());
    }

    private FormattingData createReply(String replyMessage, BlankUser user,
	    int amount) {
	MessageType replyMessageType = CustomMessageType
		.builder()
		.key(GenericFormatDataKey.USER)
		.key(GenericFormatDataKey.USER_MENTION)
		.key(ItemFormatDataKey.ITEM_AMOUNT)
		.format(replyMessage)
		.build();

	return blankUserService
		.createFormattingData(user, replyMessageType)
		.dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
		.build();
    }

    private FormattingData error(BlankUser user, int itemId,
	    String missingConfigKey) {
	return blankUserService
		.createFormattingData(user, GenericMessageType.ERROR_MESSAGE)
		.dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
			"Item '" + itemId + "' has a wrong " + missingConfigKey
				+ " configured!")
		.build();
    }

}
