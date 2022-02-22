package com.blank.humanity.discordbot.commands.items;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.item.Item;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class InventoryCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "inventory";
    }

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected SlashCommandData createCommandData(SlashCommandData commandData) {
        commandData
            .addOption(OptionType.USER, "user",
                getCommandDefinition().getOptionDescription("user"));
        return commandData;
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
        Member discordUser = Optional
            .ofNullable(event.getOption("user"))
            .map(OptionMapping::getAsMember)
            .orElse(event.getMember());
        BlankUser user = getBlankUserService().getUser(discordUser);

        FormattingData.FormattingDataBuilder builder = blankUserService
            .createFormattingData(user, null);

        String inventoryDisplay = user
            .getItems()
            .stream()
            .map(item -> generateItemDescription(item, builder))
            .collect(Collectors.joining("\n"));

        FormattingData inventoryViewer = blankUserService
            .createFormattingData(user, ItemMessageType.INVENTORY_DISPLAY)
            .dataPairing(ItemFormatDataKey.INVENTORY_BODY, inventoryDisplay)
            .build();

        reply(event, inventoryViewer);
    }

    private String generateItemDescription(Item item,
        FormattingData.FormattingDataBuilder formatBuilder) {
        Optional<ItemDefinition> itemDefinition = inventoryService
            .getItemDefinition(item.getItemId());

        if (itemDefinition.isEmpty()) {
            return "ERROR - Item ID " + item.getItemId()
                + " has not been found! Please contact an Administrator!";
        }

        ItemDefinition definition = itemDefinition.get();
        formatBuilder
            .dataPairing(ItemFormatDataKey.ITEM_ID, definition.getId())
            .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, item.getAmount())
            .dataPairing(ItemFormatDataKey.ITEM_NAME, definition.getName())
            .dataPairing(ItemFormatDataKey.ITEM_DESCRIPTION,
                definition.getDescription());
        if (definition.getUseName() == null) {
            return format(formatBuilder
                .messageType(ItemMessageType.INVENTORY_ITEM_DESCRIPTION)
                .build());
        } else {
            return format(formatBuilder
                .dataPairing(ItemFormatDataKey.ITEM_USE_NAME,
                    definition.getUseName())
                .messageType(
                    ItemMessageType.INVENTORY_ITEM_DESCRIPTION_WITH_USE)
                .build());
        }
    }
}
