package com.blank.humanity.discordbot.commands.items;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class RemoveItemCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "remove-item";
    }

    private static final String USER = "user";
    private static final String ITEM = "item";
    private static final String AMOUNT = "amount";

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected SlashCommandData createCommandData(SlashCommandData commandData) {
        commandData
            .addOption(OptionType.USER, USER,
                getCommandDefinition().getOptionDescription(USER),
                true);
        commandData
            .addOption(OptionType.STRING, ITEM,
                getCommandDefinition().getOptionDescription(ITEM),
                true);
        OptionData data = new OptionData(OptionType.INTEGER, AMOUNT,
            getCommandDefinition().getOptionDescription(AMOUNT), false);
        data.setMinValue(1);
        commandData.addOptions(data);

        return commandData;
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
        BlankUser user = getBlankUserService().getUser(event);
        BlankUser mentioned = getBlankUserService()
            .getUser(event.getOption(USER));
        String itemName = event.getOption(ITEM).getAsString();
        int amount = Optional
            .ofNullable(event.getOption(AMOUNT))
            .map(OptionMapping::getAsLong)
            .orElse(1L)
            .intValue();

        Optional<ItemDefinition> item = inventoryService
            .getItemDefinition(itemName);

        if (item.isEmpty()) {
            reply(event, getBlankUserService()
                .createFormattingData(user, ItemMessageType.ITEM_NOT_EXISTS)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, itemName)
                .build());
            return;
        }

        boolean success = inventoryService
            .removeItem(mentioned, item.get().getId(), amount);

        if (!success) {
            reply(event, getBlankUserService()
                .createFormattingData(mentioned,
                    ItemMessageType.ITEM_GIVE_NOT_ENOUGH_OWNED)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, itemName)
                .dataPairing(ItemFormatDataKey.ITEM_ID, item.get().getId())
                .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
                .build());
            return;
        }

        FormattingData data = getBlankUserService()
            .addUserDetailsFormattingData(
                getBlankUserService()
                    .createFormattingData(user,
                        ItemMessageType.ITEM_REMOVE_SUCCESS),
                mentioned, GenericFormatDataKey.RECEIVING_USER,
                GenericFormatDataKey.RECEIVING_USER_MENTION)
            .dataPairing(ItemFormatDataKey.ITEM_ID, item.get().getId())
            .dataPairing(ItemFormatDataKey.ITEM_NAME, item.get().getName())
            .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
            .build();

        reply(event, data);
    }
}
