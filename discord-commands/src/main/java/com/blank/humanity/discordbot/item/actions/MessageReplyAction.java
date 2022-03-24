package com.blank.humanity.discordbot.item.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.CustomMessageType;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

@Component
public class MessageReplyAction implements ExecutableItemAction {

    @Autowired
    private BlankUserService blankUserService;

    @Override
    public ItemActionStatus executeAction(BlankUser user,
        ItemActionState itemActionState) {

        String message = itemActionState.getStringProperty("message");

        ItemDefinition item = itemActionState.getItemDefinition();

        if (message == null) {
            itemActionState
                .reply(
                    error(blankUserService, user, item.getId(), "message"));
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        itemActionState
            .reply(createMessage(message, user, itemActionState.getAmount()));

        return ItemActionStatus.SUCCESS;
    }

    private FormattingData createMessage(String replyMessage, BlankUser user,
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

}
