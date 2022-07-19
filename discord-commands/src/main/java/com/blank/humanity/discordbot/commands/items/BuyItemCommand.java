package com.blank.humanity.discordbot.commands.items;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.aop.Argument;
import com.blank.humanity.discordbot.aop.DiscordCommand;
import com.blank.humanity.discordbot.commands.AbstractCommand;
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

import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@DiscordCommand("buy")
@Argument(name = "item", type = OptionType.STRING, autocomplete = true)
@Argument(name = "amount", type = OptionType.INTEGER, required = false, minValue = 1)
public class BuyItemCommand extends AbstractCommand {

    private static final String AMOUNT = "amount";
    private static final String ITEM = "item";

    @Setter(onMethod = @__({ @Autowired }))
    private ShopService shopService;

    @Setter(onMethod = @__({ @Autowired }))
    private ItemConfiguration itemConfiguration;

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        BlankUser user = getUser();

        Optional<ShopItem> shopItem = shopService
            .getShopItem(event.getOption(ITEM, OptionMapping::getAsString));

        int amount = event
            .getOption(AMOUNT, () -> 1l, OptionMapping::getAsLong)
            .intValue();

        if (shopItem.isEmpty()) {
            FormattingData data = getBlankUserService()
                .createFormattingData(user, ItemMessageType.ITEM_NOT_EXISTS)
                .dataPairing(ItemFormatDataKey.ITEM_NAME,
                    event.getOption(ITEM, OptionMapping::getAsString))
                .build();
            reply(data);
            return;
        }

        ShopItem item = shopItem.get();
        ItemBuyStatus status = shopService.buyItem(user, item, amount);

        MessageType messageType = switch (status) {
        case NO_AVAILABLE_SUPPLY -> ItemMessageType.BUY_ITEM_NO_SUPPLY;
        case NOT_ENOUGH_MONEY -> ItemMessageType.BUY_ITEM_NOT_ENOUGH_MONEY;
        case SUCCESS -> ItemMessageType.BUY_ITEM_SUCCESS;
        };

        FormattingData data = getBlankUserService()
            .createFormattingData(user, messageType)
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_BUY_NAME,
                item.getBuyName())
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_ID, item.getItemId())
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_PRICE,
                item.getPrice() * amount)
            .dataPairing(ItemFormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT,
                shopService.getAvailableItemAmount(item))
            .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
            .dataPairing(ItemFormatDataKey.ITEM_NAME,
                itemConfiguration
                    .getItemDefinition(item.getItemId())
                    .map(ItemDefinition::getName)
                    .orElse("NAME_ERROR"))
            .build();

        reply(data);
    }

    @Override
    @NonNull
    protected Collection<Command.Choice> onAutoComplete(
        CommandAutoCompleteInteractionEvent event) {
        String itemName = event
            .getOption(ITEM, "", OptionMapping::getAsString)
            .toLowerCase();

        return shopService
            .autoCompleteShopItems(itemName);
    }
}
