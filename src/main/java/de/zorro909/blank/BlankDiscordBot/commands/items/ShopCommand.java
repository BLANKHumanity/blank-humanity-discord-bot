package de.zorro909.blank.BlankDiscordBot.commands.items;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemConfiguration;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.config.items.ShopItem;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.ShopService;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData.FormattingDataBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class ShopCommand extends AbstractCommand {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Override
    protected CommandData createCommandData() {
	CommandData shopCommand = new CommandData("shop",
		"Displays all buyable Items from the Shop");
	shopCommand.addOption(OptionType.INTEGER, "page", "Shop-Page");
	return shopCommand;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	int page = Optional
		.ofNullable(event.getOption("page"))
		.map(OptionMapping::getAsLong)
		.orElse(1L)
		.intValue();

	if (page < 1 || page > shopService.amountShopPages()) {
	    reply(event,
		    blankUserService
			    .createSimpleFormattingData(event,
				    MessageType.SHOP_COMMAND_WRONG_PAGE));
	    return;
	}
	BlankUser blankUser = blankUserService.getUser(event);

	FormattingDataBuilder formatBuilder = blankUserService
		.createFormattingData(blankUser, null);
	formatBuilder.dataPairing(FormatDataKey.SHOP_PAGE, page);

	EmbedBuilder embedBuilder = new EmbedBuilder();

	embedBuilder
		.setTitle(format(formatBuilder
			.messageType(MessageType.SHOP_TITLE_MESSAGE)
			.build()));

	String shopDescription = format(
		formatBuilder.messageType(MessageType.SHOP_HEADER).build())
		+ "\n";

	shopDescription += shopService
		.getShopPage(page)
		.stream()
		.sequential()
		.map(item -> generateShopItemDescription(item, formatBuilder))
		.collect(Collectors.joining("\n"));

	shopDescription += "\n" + format(
		formatBuilder.messageType(MessageType.SHOP_FOOTER).build());

	embedBuilder.setDescription(shopDescription);

	reply(event, embedBuilder.build());
    }

    private String generateShopItemDescription(ShopItem item,
	    FormattingDataBuilder formatBuilder) {
	formatBuilder
		.dataPairing(FormatDataKey.SHOP_ITEM_ID, item.getId())
		.dataPairing(FormatDataKey.SHOP_ITEM_BUY_NAME,
			item.getBuyName())
		.dataPairing(FormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT,
			shopService.getAvailableItemAmount(item))
		.dataPairing(FormatDataKey.SHOP_ITEM_PRICE, item.getPrice());

	Optional<ItemDefinition> itemDefinition = itemConfiguration
		.getItemDefinition(item.getItemId());

	if (itemDefinition.isEmpty()) {
	    return "ERROR - Item ID " + item.getItemId()
		    + " has not been found! Please contact an Administrator!";
	}

	ItemDefinition definition = itemDefinition.get();
	formatBuilder.dataPairing(FormatDataKey.ITEM_ID, definition.getId());
	formatBuilder
		.dataPairing(FormatDataKey.ITEM_NAME, definition.getName());
	formatBuilder
		.dataPairing(FormatDataKey.ITEM_DESCRIPTION,
			definition.getDescription());

	return format(formatBuilder
		.messageType(MessageType.SHOP_ITEM_DESCRIPTION)
		.build());
    }

}
