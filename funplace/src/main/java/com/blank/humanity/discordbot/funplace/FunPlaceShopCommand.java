package com.blank.humanity.discordbot.funplace;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class FunPlaceShopCommand extends AbstractCommand {

    public FunPlaceShopCommand() {
	super("funshop");
    }

    @Autowired
    private FunPlaceShopService funPlaceShopService;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Override
    protected CommandData createCommandData(CommandData shopCommand) {
	OptionData page = new OptionData(OptionType.INTEGER, "page",
		getCommandDefinition().getOptionDescription("page"));
	page.setMinValue(1);
	page.setMaxValue(funPlaceShopService.amountShopPages());
	shopCommand.addOptions(page);
	return shopCommand;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	int page = Optional
		.ofNullable(event.getOption("page"))
		.map(OptionMapping::getAsLong)
		.orElse(1L)
		.intValue();

	if (page < 1 || page > funPlaceShopService.amountShopPages()) {
	    reply(event, blankUserService
		    .createSimpleFormattingData(event,
			    FunPlaceMessageType.SHOP_COMMAND_WRONG_PAGE));
	    return;
	}
	BlankUser blankUser = blankUserService.getUser(event);

	FormattingData.FormattingDataBuilder formatBuilder = blankUserService
		.createFormattingData(blankUser, null);
	formatBuilder.dataPairing(FunPlaceFormatDataKey.SHOP_PAGE, page);

	EmbedBuilder embedBuilder = new EmbedBuilder();

	embedBuilder
		.setTitle(format(formatBuilder
			.messageType(
				FunPlaceMessageType.FUN_PLACE_SHOP_TITLE_MESSAGE)
			.build()));

	String shopDescription = format(formatBuilder
		.messageType(FunPlaceMessageType.FUN_PLACE_SHOP_HEADER)
		.build()) + "\n";

	shopDescription += funPlaceShopService
		.getShopPage(page)
		.stream()
		.sequential()
		.map(item -> generateShopItemDescription(item, formatBuilder))
		.collect(Collectors.joining("\n"));

	shopDescription += "\n" + format(formatBuilder
		.messageType(FunPlaceMessageType.FUN_PLACE_SHOP_FOOTER)
		.build());

	embedBuilder.setDescription(shopDescription);

	reply(event, embedBuilder.build());
    }

    private String generateShopItemDescription(ShopItem item,
	    FormattingData.FormattingDataBuilder formatBuilder) {
	formatBuilder
		.dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_ID, item.getId())
		.dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_BUY_NAME,
			item.getBuyName())
		.dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT,
			funPlaceShopService.getAvailableItemAmount(item))
		.dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_PRICE,
			item.getPrice());

	Optional<ItemDefinition> itemDefinition = itemConfiguration
		.getItemDefinition(item.getItemId());

	if (itemDefinition.isEmpty()) {
	    return "ERROR - Item ID " + item.getItemId()
		    + " has not been found! Please contact an Administrator!";
	}

	ItemDefinition definition = itemDefinition.get();
	formatBuilder
		.dataPairing(FunPlaceFormatDataKey.ITEM_ID, definition.getId());
	formatBuilder
		.dataPairing(FunPlaceFormatDataKey.ITEM_NAME,
			definition.getName());
	formatBuilder
		.dataPairing(FunPlaceFormatDataKey.ITEM_DESCRIPTION,
			definition.getDescription());

	return format(formatBuilder
		.messageType(FunPlaceMessageType.SHOP_ITEM_DESCRIPTION)
		.build());
    }

}
