package com.blank.humanity.discordbot.funplace;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;

import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class FunPlaceBuyItemCommand extends AbstractCommand {

    private static final String AMOUNT = "amount";
    private static final String ITEM = "item";

    @Setter(onMethod = @__({ @Autowired }))
    private FunPlaceShopService shopService;

    @Setter(onMethod = @__({ @Autowired }))
    private ItemConfiguration itemConfiguration;

    @Override
    public String getCommandName() {
        return "funbuy";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.STRING, ITEM,
                definition.getOptionDescription(ITEM), true, true);
        OptionData amount = new OptionData(OptionType.INTEGER, AMOUNT,
            definition.getOptionDescription(AMOUNT));
        amount.setMinValue(1);
        commandData.addOptions(amount);
        return commandData;
    }

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
                .createFormattingData(user,
                    FunPlaceMessageType.ITEM_NOT_EXISTS)
                .dataPairing(FunPlaceFormatDataKey.ITEM_NAME,
                    event.getOption("item").getAsString())
                .build();
            reply(data);
            return;
        }

        ShopItem item = shopItem.get();
        ItemBuyStatus status = shopService.buyItem(user, item, amount);

        MessageType messageType = switch (status) {
        case NO_AVAILABLE_SUPPLY -> FunPlaceMessageType.BUY_ITEM_NO_SUPPLY;
        case NOT_ENOUGH_MONEY -> FunPlaceMessageType.BUY_ITEM_NOT_ENOUGH_MONEY;
        case SUCCESS -> FunPlaceMessageType.BUY_ITEM_SUCCESS;
        };

        FormattingData data = getBlankUserService()
            .createFormattingData(user, messageType)
            .dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_BUY_NAME,
                item.getBuyName())
            .dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_ID,
                item.getItemId())
            .dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_PRICE,
                item.getPrice())
            .dataPairing(FunPlaceFormatDataKey.SHOP_ITEM_AVAILABLE_AMOUNT,
                shopService.getAvailableItemAmount(item))
            .dataPairing(FunPlaceFormatDataKey.ITEM_AMOUNT, amount)
            .dataPairing(FunPlaceFormatDataKey.ITEM_NAME,
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
        @NonNull CommandAutoCompleteInteractionEvent event) {
        String itemName = event
            .getOption(ITEM, () -> "", OptionMapping::getAsString)
            .toLowerCase();

        return shopService
            .autoCompleteShopItems(itemName);
    }

}
