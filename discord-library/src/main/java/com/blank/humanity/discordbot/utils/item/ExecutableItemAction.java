package com.blank.humanity.discordbot.utils.item;

import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.item.actions.ItemActionState;
import com.blank.humanity.discordbot.item.actions.ItemActionStatus;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.utils.FormattingData;

public interface ExecutableItemAction {

    public ItemActionStatus executeAction(BlankUser user,
        ItemActionState itemActionState);

    default FormattingData error(BlankUserService userService,
        BlankUser user, int itemId, String missingConfigKey) {
        return userService
            .createFormattingData(user, GenericMessageType.ERROR_MESSAGE)
            .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                "Item '" + itemId + "' has a wrong " + missingConfigKey
                    + " configured!")
            .build();
    }

}
