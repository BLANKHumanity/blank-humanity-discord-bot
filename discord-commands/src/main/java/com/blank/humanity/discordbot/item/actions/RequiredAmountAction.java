package com.blank.humanity.discordbot.item.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.item.actions.messages.ItemActionMessageType;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

@Component
public class RequiredAmountAction implements ExecutableItemAction {

    @Autowired
    private BlankUserService blankUserService;

    @Override
    public ItemActionStatus executeAction(BlankUser user,
        ItemActionState itemActionState) {
        int requiredAmount = itemActionState
            .getProperty("amount", Integer::parseInt, 1);

        ItemDefinition item = itemActionState.getItemDefinition();

        if (itemActionState.getAmount() != requiredAmount) {
            itemActionState
                .reply(blankUserService
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
        return ItemActionStatus.SUCCESS;
    }

}
