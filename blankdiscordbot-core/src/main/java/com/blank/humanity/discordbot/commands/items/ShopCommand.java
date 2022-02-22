package com.blank.humanity.discordbot.commands.items;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.items.ShopItem;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.ShopService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class ShopCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "shop";
    }

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Override
    protected SlashCommandData createCommandData(SlashCommandData shopCommand) {
        OptionData page = new OptionData(OptionType.INTEGER, "page",
            getCommandDefinition().getOptionDescription("page"));
        page.setMinValue(1);
        page.setMaxValue(shopService.amountShopPages());
        shopCommand.addOptions(page);
        return shopCommand;
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
        int page = Optional
            .ofNullable(event.getOption("page"))
            .map(OptionMapping::getAsLong)
            .orElse(1L)
            .intValue();

        if (page < 1 || page > shopService.amountShopPages()) {
            reply(event,
                blankUserService
                    .createSimpleFormattingData(event,
                        ItemMessageType.SHOP_COMMAND_WRONG_PAGE));
            return;
        }
        BlankUser blankUser = blankUserService.getUser(event);

        FormattingData.FormattingDataBuilder formatBuilder = blankUserService
            .createFormattingData(blankUser, null);
        formatBuilder.dataPairing(ItemFormatDataKey.SHOP_PAGE, page);

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder
            .setTitle(format(formatBuilder
                .messageType(ItemMessageType.SHOP_TITLE_MESSAGE)
                .build()));

        String shopDescription = format(
            formatBuilder.messageType(ItemMessageType.SHOP_HEADER).build())
            + "\n";

        shopDescription += shopService
            .getShopPage(page)
            .stream()
            .sequential()
            .map(item -> generateShopItemDescription(item, formatBuilder))
            .collect(Collectors.joining("\n"));

        shopDescription += "\n" + format(
            formatBuilder.messageType(ItemMessageType.SHOP_FOOTER).build());

        embedBuilder.setDescription(shopDescription);

        reply(event, embedBuilder.build());
    }

    private String generateShopItemDescription(ShopItem item,
        FormattingData.FormattingDataBuilder formatBuilder) {
        formatBuilder
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_ID, item.getId())
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_BUY_NAME,
                item.getBuyName())
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT,
                shopService.getAvailableItemAmount(item))
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_PRICE,
                item.getPrice());

        Optional<ItemDefinition> itemDefinition = itemConfiguration
            .getItemDefinition(item.getItemId());

        if (itemDefinition.isEmpty()) {
            return "ERROR - Item ID " + item.getItemId()
                + " has not been found! Please contact an Administrator!";
        }

        ItemDefinition definition = itemDefinition.get();
        formatBuilder
            .dataPairing(ItemFormatDataKey.ITEM_ID, definition.getId());
        formatBuilder
            .dataPairing(ItemFormatDataKey.ITEM_NAME, definition.getName());
        formatBuilder
            .dataPairing(ItemFormatDataKey.ITEM_DESCRIPTION,
                definition.getDescription());

        return format(formatBuilder
            .messageType(ItemMessageType.SHOP_ITEM_DESCRIPTION)
            .build());
    }

}
