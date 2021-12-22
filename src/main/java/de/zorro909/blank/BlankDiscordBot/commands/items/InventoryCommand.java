package de.zorro909.blank.BlankDiscordBot.commands.items;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.Item;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData.FormattingDataBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class InventoryCommand extends AbstractCommand {

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected CommandData createCommandData() {
	CommandData commandData = new CommandData("inventory",
		"Displays your Inventory");
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = blankUserService.getUser(event);

	FormattingDataBuilder builder = blankUserService
		.createFormattingData(user);

	String inventoryDisplay = user
		.getItems()
		.stream()
		.map(item -> generateItemDescription(item, builder))
		.collect(Collectors.joining("\n"));

	reply(event, "Inventory\n" + inventoryDisplay, builder.build());
    }

    private String generateItemDescription(Item item,
	    FormattingDataBuilder formatBuilder) {
	Optional<ItemDefinition> itemDefinition = inventoryService
		.getItemDefinition(item.getItemId());

	if (itemDefinition.isEmpty()) {
	    return "ERROR - Item ID " + item.getItemId()
		    + " has not been found! Please contact an Administrator!";
	}

	ItemDefinition definition = itemDefinition.get();
	formatBuilder
		.dataPairing(FormatDataKey.ITEM_ID, definition.getId())
		.dataPairing(FormatDataKey.ITEM_AMOUNT, item.getAmount())
		.dataPairing(FormatDataKey.ITEM_NAME, definition.getName())
		.dataPairing(FormatDataKey.ITEM_USE_NAME,
			definition.getUseName())
		.dataPairing(FormatDataKey.ITEM_DESCRIPTION,
			definition.getDescription());
	if(definition.getUseName() == null) {
	    return format(messagesConfig.INVENTORY_ITEM_DESCRIPTION,
			formatBuilder.build());
	}else {
	    return format(messagesConfig.INVENTORY_ITEM_DESCRIPTION_WITH_USE,
			formatBuilder.build());
	}
    }
}
