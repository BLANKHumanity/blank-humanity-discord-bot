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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class GiveItemCommand extends AbstractCommand {

    public GiveItemCommand() {
	super("give");
    }

    private static final String USER = "user";
    private static final String ITEM = "item";
    private static final String AMOUNT = "amount";

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
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
    protected void onCommand(SlashCommandEvent event) {
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

	if (!inventoryService.removeItem(user, item.get().getId(), amount)) {
	    reply(event, getBlankUserService()
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

	reply(event, data);
    }

}
