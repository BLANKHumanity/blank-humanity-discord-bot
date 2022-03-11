package com.blank.humanity.discordbot.commands.items;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.item.Item;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class InventoryCommand extends AbstractCommand {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public String getCommandName() {
        return "inventory";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.USER, "user",
                definition.getOptionDescription("user"));
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        BlankUser user = event
            .getOption("user", this::getUser, getBlankUserService()::getUser);

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

        reply(inventoryViewer);
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
            return getMessageService()
                .format(formatBuilder
                    .messageType(ItemMessageType.INVENTORY_ITEM_DESCRIPTION)
                    .build());
        } else {
            return getMessageService()
                .format(formatBuilder
                    .dataPairing(ItemFormatDataKey.ITEM_USE_NAME,
                        definition.getUseName())
                    .messageType(
                        ItemMessageType.INVENTORY_ITEM_DESCRIPTION_WITH_USE)
                    .build());
        }
    }
}
