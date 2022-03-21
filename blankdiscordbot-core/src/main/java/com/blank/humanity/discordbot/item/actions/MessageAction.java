package com.blank.humanity.discordbot.item.actions;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.CustomMessageType;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.item.actions.messages.ItemActionMessageType;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.NamedFormatter;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

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
