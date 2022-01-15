package de.zorro909.blank.BlankDiscordBot.commands.items;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.item.Item;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData.FormattingDataBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class InventoryCommand extends AbstractCommand {

    public InventoryCommand() {
	super("inventory");
    }

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData
		.addOption(OptionType.USER, "user",
			getCommandDefinition().getOptionDescription("user"));
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	Member discordUser = Optional
		.ofNullable(event.getOption("user"))
		.map(OptionMapping::getAsMember)
		.orElse(event.getMember());
	BlankUser user = getBlankUserService().getUser(discordUser);

	FormattingDataBuilder builder = blankUserService
		.createFormattingData(user, null);

	String inventoryDisplay = user
		.getItems()
		.stream()
		.map(item -> generateItemDescription(item, builder))
		.collect(Collectors.joining("\n"));

	FormattingData inventoryViewer = blankUserService
		.createFormattingData(user, MessageType.INVENTORY_DISPLAY)
		.dataPairing(FormatDataKey.INVENTORY_BODY, inventoryDisplay)
		.build();

	reply(event, inventoryViewer);
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
		.dataPairing(FormatDataKey.ITEM_DESCRIPTION,
			definition.getDescription());
	if (definition.getUseName() == null) {
	    return format(formatBuilder
		    .messageType(MessageType.INVENTORY_ITEM_DESCRIPTION)
		    .build());
	} else {
	    return format(formatBuilder
		    .dataPairing(FormatDataKey.ITEM_USE_NAME,
			    definition.getUseName())
		    .messageType(
			    MessageType.INVENTORY_ITEM_DESCRIPTION_WITH_USE)
		    .build());
	}
    }
}
