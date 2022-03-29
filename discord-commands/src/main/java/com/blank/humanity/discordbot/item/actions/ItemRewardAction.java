package com.blank.humanity.discordbot.item.actions;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.config.items.ActionConfigSelector;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

@Component
public class ItemRewardAction implements ExecutableItemAction {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private BlankUserService blankUserService;

    @Override
    public ItemActionStatus executeAction(BlankUser user,
        ItemActionState itemActionState) {

        String itemName = itemActionState.getStringProperty("itemName", "");
        Integer itemId = itemActionState
            .getProperty("itemId", Integer::parseInt, -1);

        Optional<ItemDefinition> item = Optional.empty();

        if (!itemName.isBlank()) {
            item = inventoryService.getItemDefinition(itemName);
        }
        if (itemId != -1 && item.isEmpty()) {
            item = inventoryService.getItemDefinition(itemId);
        }

        if (item.isEmpty()) {
            itemActionState
                .reply(error(blankUserService, user,
                    itemActionState.getItemDefinition().getId(),
                    "itemName or itemId"));
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        int amount = itemActionState
            .getProperty("amount", Integer::parseInt, 1);

        ItemDefinition itemDefinition = item.get();

        itemActionState
            .setSelector(ActionConfigSelector.ITEM_ID, itemDefinition.getId());
        itemActionState
            .setSelector(ActionConfigSelector.ITEM_NAME,
                itemDefinition.getName());
        itemActionState
            .setSelector(ActionConfigSelector.ITEM_USE_NAME,
                itemDefinition.getUseName());
        itemActionState.setSelector(ActionConfigSelector.ITEM_AMOUNT, amount);

        inventoryService.giveItem(user, itemDefinition.getId(), amount);

        return ItemActionStatus.SUCCESS;
    }

}
