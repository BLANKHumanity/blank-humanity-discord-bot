package com.blank.humanity.discordbot.commands.items;

import java.util.Collection;
import java.util.Optional;

import com.blank.humanity.discordbot.aop.DiscordCommand;
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

@DiscordCommand("gift")
public class GiftItemCommand extends ItemInteractionCommand {

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        String itemName = event.getOption(ITEM, OptionMapping::getAsString);

        int amount = event
            .getOption(AMOUNT, 1L, OptionMapping::getAsLong)
            .intValue();

        Optional<ItemDefinition> item = inventoryService
            .getItemDefinition(itemName);

        if (item.isEmpty()) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    ItemMessageType.ITEM_NOT_EXISTS)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, itemName)
                .build());
            return;
        }

        BlankUser mentioned = event
            .getOption(USER, getBlankUserService()::getUser);

        inventoryService.giveItem(mentioned, item.get().getId(), amount);

        FormattingData.FormattingDataBuilder builder = getBlankUserService()
            .createFormattingData(getUser(), ItemMessageType.ITEM_GIVE_SUCCESS);
        builder = getBlankUserService()
            .addUserDetailsFormattingData(builder, mentioned,
                GenericFormatDataKey.RECEIVING_USER,
                GenericFormatDataKey.RECEIVING_USER_MENTION);
        FormattingData data = builder
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
            .autoCompleteItems(itemName);
    }

}
