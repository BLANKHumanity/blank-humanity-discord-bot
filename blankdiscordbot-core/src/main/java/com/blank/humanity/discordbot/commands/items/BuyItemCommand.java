package com.blank.humanity.discordbot.commands.items;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.items.ShopItem;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.ShopService;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.item.ItemBuyStatus;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class BuyItemCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "buy";
    }

    private static final String AMOUNT = "amount";
    private static final String ITEM = "item";

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Override
    protected SlashCommandData createCommandData(SlashCommandData commandData) {
        commandData
            .addOption(OptionType.STRING, ITEM,
                getCommandDefinition().getOptionDescription(ITEM),
                true);
        OptionData amount = new OptionData(OptionType.INTEGER, AMOUNT,
            getCommandDefinition().getOptionDescription(AMOUNT));
        amount.setMinValue(1);
        commandData.addOptions(amount);
        return commandData;
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
        BlankUser user = blankUserService.getUser(event);

        Optional<ShopItem> shopItem = shopService
            .getShopItem(event.getOption("item").getAsString());

        int amount = Optional
            .ofNullable(event.getOption(AMOUNT))
            .map(OptionMapping::getAsLong)
            .orElse(1L)
            .intValue();

        if (shopItem.isEmpty()) {
            FormattingData data = blankUserService
                .createFormattingData(user, ItemMessageType.ITEM_NOT_EXISTS)
                .dataPairing(ItemFormatDataKey.ITEM_NAME,
                    event.getOption(ITEM).getAsString())
                .build();
            reply(event, data);
            return;
        }

        ShopItem item = shopItem.get();
        ItemBuyStatus status = shopService.buyItem(user, item, amount);

        MessageType messageType = switch (status) {
        case NO_AVAILABLE_SUPPLY -> ItemMessageType.BUY_ITEM_NO_SUPPLY;
        case NOT_ENOUGH_MONEY -> ItemMessageType.BUY_ITEM_NOT_ENOUGH_MONEY;
        case SUCCESS -> ItemMessageType.BUY_ITEM_SUCCESS;
        };

        FormattingData data = blankUserService
            .createFormattingData(user, messageType)
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_BUY_NAME,
                item.getBuyName())
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_ID, item.getItemId())
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_PRICE, item.getPrice())
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT,
                shopService.getAvailableItemAmount(item))
            .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
            .dataPairing(EconomyFormatDataKey.BALANCE, user.getBalance())
            .dataPairing(ItemFormatDataKey.ITEM_NAME,
                itemConfiguration
                    .getItemDefinition(item.getItemId())
                    .map(ItemDefinition::getName)
                    .orElse("NAME_ERROR"))
            .build();

        reply(event, data);
    }

}
