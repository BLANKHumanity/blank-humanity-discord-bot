package de.zorro909.blank.BlankDiscordBot.commands.items;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemConfiguration;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.config.items.ShopItem;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.ShopService;
import de.zorro909.blank.BlankDiscordBot.services.item.ItemBuyStatus;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class BuyItemCommand extends AbstractCommand {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Override
    protected CommandData createCommandData() {
	CommandData commandData = new CommandData("buy", "Buys an Item");
	commandData
		.addOption(OptionType.STRING, "item",
			"The name of the Shop Item to buy", true);
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = blankUserService.getUser(event);

	Optional<ShopItem> shopItem = shopService
		.getShopItem(event.getOption("item").getAsString());

	if (shopItem.isEmpty()) {
	    FormattingData data = blankUserService
		    .createFormattingData(user)
		    .dataPairing(FormatDataKey.ITEM_NAME,
			    event.getOption("item").getAsString())
		    .build();
	    reply(event, messagesConfig.ITEM_NOT_EXISTS, data);
	    return;
	}

	ShopItem item = shopItem.get();
	ItemBuyStatus status = shopService.buyItem(user, item);

	String description = switch (status) {
	case NO_AVAILABLE_SUPPLY -> messagesConfig.BUY_ITEM_NO_SUPPLY;
	case NOT_ENOUGH_MONEY -> messagesConfig.BUY_ITEM_NOT_ENOUGH_MONEY;
	case SUCCESS -> messagesConfig.BUY_ITEM_SUCCESS;
	};

	FormattingData data = blankUserService
		.createFormattingData(user)
		.dataPairing(FormatDataKey.SHOP_ITEM_BUY_NAME,
			item.getBuyName())
		.dataPairing(FormatDataKey.SHOP_ITEM_ID, item.getItemId())
		.dataPairing(FormatDataKey.SHOP_ITEM_PRICE, item.getPrice())
		.dataPairing(FormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT,
			shopService.getAvailableItemAmount(item))
		.dataPairing(FormatDataKey.BALANCE, user.getBalance())
		.dataPairing(FormatDataKey.ITEM_NAME,
			itemConfiguration
				.getItemDefinition(item.getItemId())
				.map(ItemDefinition::getName)
				.orElse("NAME_ERROR"))
		.build();

	reply(event, description, data);
    }

}
