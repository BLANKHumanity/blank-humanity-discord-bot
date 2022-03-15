package com.blank.humanity.discordbot.commands.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.commands.items.BuyItemCommand;
import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.items.ShopItem;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.ShopService;
import com.blank.humanity.discordbot.utils.item.ItemBuyStatus;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class BuyItemCommandTest extends CommandUnitTest<BuyItemCommand> {

    @Mock
    private ShopService shopService;

    @Mock
    private ItemConfiguration itemConfiguration;

    protected BuyItemCommandTest() {
        super(BuyItemCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions())
            .hasSize(2)
            .anyMatch(hasOption("item", OptionType.STRING, true, true))
            .anyMatch(hasOption("amount", OptionType.INTEGER, false));
    }

    @BeforeEach
    void setupShopService() {
        commandMock.setShopService(shopService);
        commandMock.setItemConfiguration(itemConfiguration);
    }

    @Test
    void testBuyItem(@Mock BlankUser user) {
        String itemName = "testExampleItem";
        ItemDefinition itemDefinition = new ItemDefinition();
        itemDefinition.setId(2);
        itemDefinition.setName("Test Example Item");
        ShopItem item = new ShopItem(1, 2, itemName, 312, 21, true, 1);

        when(shopService.getShopItem(itemName)).thenReturn(Optional.of(item));
        when(shopService.buyItem(user, item, 1))
            .thenReturn(ItemBuyStatus.SUCCESS);
        when(itemConfiguration.getItemDefinition(itemDefinition.getId()))
            .thenReturn(Optional.of(itemDefinition));

        mockServiceCreateFormatting(user, ItemMessageType.BUY_ITEM_SUCCESS);

        mockMessageFormats(ItemMessageType.BUY_ITEM_SUCCESS,
            "%(shopItemId):%(shopItemBuyName):%(itemName):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "item", itemName));

        String expectedBody = item.getItemId() + ":" + item.getBuyName() + ":"
            + itemDefinition.getName() + ":1";

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(expectedBody));
    }

    @Test
    void testBuyMultipleItems(@Mock BlankUser user) {
        String itemName = "testExampleItem";
        int amount = 6;
        ItemDefinition itemDefinition = new ItemDefinition();
        itemDefinition.setId(2);
        itemDefinition.setName("Test Example Item");
        ShopItem item = new ShopItem(1, 2, itemName, 312, 21, true, 1);

        when(shopService.getShopItem(itemName)).thenReturn(Optional.of(item));
        when(shopService.buyItem(user, item, amount))
            .thenReturn(ItemBuyStatus.SUCCESS);
        when(itemConfiguration.getItemDefinition(itemDefinition.getId()))
            .thenReturn(Optional.of(itemDefinition));

        mockServiceCreateFormatting(user, ItemMessageType.BUY_ITEM_SUCCESS);

        mockMessageFormats(ItemMessageType.BUY_ITEM_SUCCESS,
            "%(shopItemId):%(shopItemBuyName):%(itemName):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "item", itemName),
            optionMapping(OptionType.INTEGER, "amount", amount));

        String expectedBody = item.getItemId() + ":" + item.getBuyName() + ":"
            + itemDefinition.getName() + ":" + amount;

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(expectedBody));
    }

    @Test
    void testBuyItemNoSupply(@Mock BlankUser user) {
        String itemName = "testExampleItem";

        ItemDefinition itemDefinition = new ItemDefinition();
        itemDefinition.setId(2);
        itemDefinition.setName("Test Example Item");
        ShopItem item = new ShopItem(1, 2, itemName, 312, 21, true, 1);

        when(shopService.getShopItem(itemName)).thenReturn(Optional.of(item));
        when(shopService.buyItem(user, item, 1))
            .thenReturn(ItemBuyStatus.NO_AVAILABLE_SUPPLY);
        when(itemConfiguration.getItemDefinition(itemDefinition.getId()))
            .thenReturn(Optional.of(itemDefinition));

        mockServiceCreateFormatting(user, ItemMessageType.BUY_ITEM_NO_SUPPLY);

        mockMessageFormats(ItemMessageType.BUY_ITEM_NO_SUPPLY,
            "%(shopItemId):%(shopItemBuyName):%(itemName):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "item", itemName));

        String expectedBody = item.getItemId() + ":" + item.getBuyName() + ":"
            + itemDefinition.getName() + ":1";

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(expectedBody));
    }

    @Test
    void testBuyItemNotEnoughMoney(@Mock BlankUser user) {
        String itemName = "testExampleItem";

        ItemDefinition itemDefinition = new ItemDefinition();
        itemDefinition.setId(2);
        itemDefinition.setName("Test Example Item");
        ShopItem item = new ShopItem(1, 2, itemName, 312, 21, true, 1);

        when(shopService.getShopItem(itemName)).thenReturn(Optional.of(item));
        when(shopService.buyItem(user, item, 1))
            .thenReturn(ItemBuyStatus.NOT_ENOUGH_MONEY);
        when(itemConfiguration.getItemDefinition(itemDefinition.getId()))
            .thenReturn(Optional.of(itemDefinition));

        mockServiceCreateFormatting(user,
            ItemMessageType.BUY_ITEM_NOT_ENOUGH_MONEY);

        mockMessageFormats(ItemMessageType.BUY_ITEM_NOT_ENOUGH_MONEY,
            "%(shopItemId):%(shopItemBuyName):%(itemName):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "item", itemName));

        String expectedBody = item.getItemId() + ":" + item.getBuyName() + ":"
            + itemDefinition.getName() + ":1";

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(expectedBody));
    }

    @Test
    void testBuyNonExistingItem(@Mock BlankUser user) {
        String itemName = "testExampleItem";

        when(shopService.getShopItem(itemName)).thenReturn(Optional.empty());

        mockServiceCreateFormatting(user, ItemMessageType.ITEM_NOT_EXISTS);

        mockMessageFormats(ItemMessageType.ITEM_NOT_EXISTS,
            "%(itemName)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "item", itemName));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(itemName));
    }

    @Test
    void testAutoCompleteEmptyRequest() {

        CommandAutoCompleteInteractionEvent event = mockAutocompleteEvent();

        when(shopService.autoCompleteShopItems(""))
            .thenReturn(List.of(Mockito.mock(Command.Choice.class)));

        Collection<Command.Choice> choices = callAutocomplete(event);

        assertThat(choices).hasSize(1);
    }

    @Test
    void testAutoComplete() {
        String request = "dot";

        CommandAutoCompleteInteractionEvent event = mockAutocompleteEvent(
            optionMapping(OptionType.STRING, "item", request));

        when(shopService.autoCompleteShopItems(request))
            .thenReturn(List.of(Mockito.mock(Command.Choice.class)));

        Collection<Command.Choice> choices = callAutocomplete(event);

        assertThat(choices).hasSize(1);
    }

}
