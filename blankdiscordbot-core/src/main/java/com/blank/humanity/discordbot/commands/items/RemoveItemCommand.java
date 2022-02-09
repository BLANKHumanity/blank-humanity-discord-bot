package com.blank.humanity.discordbot.commands.items;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericFormatDataKey;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class RemoveItemCommand extends AbstractCommand {

    public RemoveItemCommand() {
	super("remove-item");
    }

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData
		.addOption(OptionType.USER, "user",
			getCommandDefinition().getOptionDescription("user"),
			true);
	commandData
		.addOption(OptionType.STRING, "item",
			getCommandDefinition().getOptionDescription("item"),
			true);
	OptionData data = new OptionData(OptionType.INTEGER, "amount",
		getCommandDefinition().getOptionDescription("amount"), false);
	data.setMinValue(1);
	commandData.addOptions(data);

	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = getBlankUserService().getUser(event);
	BlankUser mentioned = getBlankUserService()
		.getUser(event.getOption("user"));
	String itemName = event.getOption("item").getAsString();
	int amount = Optional
		.ofNullable(event.getOption("amount"))
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
