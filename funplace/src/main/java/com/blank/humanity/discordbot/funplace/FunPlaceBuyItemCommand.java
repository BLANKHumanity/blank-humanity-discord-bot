package com.blank.humanity.discordbot.funplace;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class FunPlaceBuyItemCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "funbuy";
    }

    @Autowired
    private FunPlaceShopService shopService;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
        commandData
            .addOption(OptionType.STRING, "item",
                getCommandDefinition().getOptionDescription("item"),
                true);
        OptionData amount = new OptionData(OptionType.INTEGER, "amount",
            getCommandDefinition().getOptionDescription("amount"));
        amount.setMinValue(1);
        commandData.addOptions(amount);
        return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
        BlankUser user = blankUserService.getUser(event);

        Optional<ShopItem> shopItem = shopService
            .getShopItem(event.getOption("item").getAsString());

        int amount = Optional
            .ofNullable(event.getOption("amount"))
            .map(OptionMapping::getAsLong)
            .orElse(1L)
            .intValue();

        if (shopItem.isEmpty()) {
            FormattingData data = blankUserService
                .createFormattingData(user,
                    FunPlaceMessageType.ITEM_NOT_EXISTS)
                .dataPairing(FunPlaceFormatDataKey.ITEM_NAME,
                    event.getOption("item").getAsString())
                .build();
            reply(event, data);
            return;
        }

        ShopItem item = shopItem.get();
        ItemBuyStatus status = shopService.buyItem(user, item, amount);

        MessageType messageType = switch (status) {
        case NO_AVAILABLE_SUPPLY -> FunPlaceMessageType.BUY_ITEM_NO_SUPPLY;
        case NOT_ENOUGH_MONEY -> FunPlaceMessageType.BUY_ITEM_NOT_ENOUGH_MONEY;
        case SUCCESS -> FunPlaceMessageType.BUY_ITEM_SUCCESS;
        };

        FormattingData data = blankUserService
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

        reply(event, data);
    }

}
