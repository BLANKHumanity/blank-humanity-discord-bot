package com.blank.humanity.discordbot.commands.items;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Component
public class GiveItemCommand extends ItemInteractionCommand {

    @Override
    public String getCommandName() {
        return "give";
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        BlankUser user = getUser();
        BlankUser mentioned = getBlankUserService()
            .getUser(event.getOption(USER));

        String itemName = event.getOption(ITEM, OptionMapping::getAsString);

        int amount = event
            .getOption(AMOUNT, 1L, OptionMapping::getAsLong)
            .intValue();

        Optional<ItemDefinition> item = inventoryService
            .getItemDefinition(itemName);

        if (item.isEmpty()) {
            reply(getBlankUserService()
                .createFormattingData(user, ItemMessageType.ITEM_NOT_EXISTS)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, itemName)
                .build());
            return;
        }

        if (!inventoryService.removeItem(user, item.get().getId(), amount)) {
            reply(getBlankUserService()
                .createFormattingData(user,
                    ItemMessageType.ITEM_GIVE_NOT_ENOUGH_OWNED)
                .dataPairing(ItemFormatDataKey.ITEM_ID, item.get().getId())
                .dataPairing(ItemFormatDataKey.ITEM_NAME,
                    item.get().getName())
                .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
                .build());
            return;
        }

        inventoryService.giveItem(mentioned, item.get().getId(), amount);

        FormattingData data = getBlankUserService()
            .addUserDetailsFormattingData(
                getBlankUserService()
                    .createFormattingData(user,
                        ItemMessageType.ITEM_GIVE_SUCCESS),
                mentioned, GenericFormatDataKey.RECEIVING_USER,
                GenericFormatDataKey.RECEIVING_USER_MENTION)
            .dataPairing(ItemFormatDataKey.ITEM_ID, item.get().getId())
            .dataPairing(ItemFormatDataKey.ITEM_NAME, item.get().getName())
            .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
            .build();

        reply(data);
    }

    @Override
    protected Collection<Command.Choice> onAutoComplete(
        CommandAutoCompleteInteractionEvent event) {
        String itemName = event
            .getOption(ITEM, () -> "", OptionMapping::getAsString)
            .toLowerCase();

        return inventoryService
            .autoCompleteUserItems(getUser(), itemName);
    }

}
