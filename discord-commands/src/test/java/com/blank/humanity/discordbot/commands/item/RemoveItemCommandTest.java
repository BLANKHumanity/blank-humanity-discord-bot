package com.blank.humanity.discordbot.commands.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.items.RemoveItemCommand;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class RemoveItemCommandTest extends CommandUnitTest<RemoveItemCommand> {

    @Mock
    private InventoryService inventoryService;

    protected RemoveItemCommandTest() {
        super(RemoveItemCommand.class);
    }

    @BeforeEach
    void setupInventoryService() {
        commandMock.setInventoryService(inventoryService);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions())
            .hasSize(3)
            .anyMatch(hasOption("user", OptionType.USER, true))
            .anyMatch(hasOption("item", OptionType.STRING, true, true))
            .anyMatch(hasOption("amount", OptionType.INTEGER, false));
    }

    @Test
    void testRemoveSingleItem(@Mock BlankUser user, @Mock Member receiver,
        @Mock BlankUser receiverUser, @Mock ItemDefinition itemDefinition) {
        String itemName = "testItem";
        int itemId = 34;

        when(itemDefinition.getId()).thenReturn(itemId);
        when(itemDefinition.getName()).thenReturn(itemName);
        when(inventoryService.getItemDefinition(itemName))
            .thenReturn(Optional.of(itemDefinition));
        when(inventoryService.removeItem(receiverUser, itemId, 1))
            .thenReturn(true);

        mockReceivingUser(receiver, receiverUser);

        mockServiceCreateFormatting(user, ItemMessageType.ITEM_REMOVE_SUCCESS);

        mockMessageFormats(ItemMessageType.ITEM_REMOVE_SUCCESS,
            "%(itemName):%(itemId):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.USER, "user", receiver),
            optionMapping(OptionType.STRING, "item", itemName));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(itemName + ":" + itemId + ":1"));

        verify(inventoryService).removeItem(receiverUser, itemId, 1);
    }

    @Test
    void testRemoveMultipleItems(@Mock BlankUser user, @Mock Member receiver,
        @Mock BlankUser receiverUser, @Mock ItemDefinition itemDefinition) {
        String itemName = "testItem";
        int itemId = 34;
        int amount = 3;

        when(itemDefinition.getId()).thenReturn(itemId);
        when(itemDefinition.getName()).thenReturn(itemName);
        when(inventoryService.getItemDefinition(itemName))
            .thenReturn(Optional.of(itemDefinition));
        when(inventoryService.removeItem(receiverUser, itemId, amount))
            .thenReturn(true);

        mockReceivingUser(receiver, receiverUser);

        mockServiceCreateFormatting(user, ItemMessageType.ITEM_REMOVE_SUCCESS);

        mockMessageFormats(ItemMessageType.ITEM_REMOVE_SUCCESS,
            "%(itemName):%(itemId):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.USER, "user", receiver),
            optionMapping(OptionType.STRING, "item", itemName),
            optionMapping(OptionType.INTEGER, "amount", amount));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(
                embedHasDescription(itemName + ":" + itemId + ":" + amount));

        verify(inventoryService).removeItem(receiverUser, itemId, amount);
    }

    @Test
    void testRemoveItemNotOwned(@Mock BlankUser user, @Mock Member receiver,
        @Mock BlankUser receiverUser, @Mock ItemDefinition itemDefinition) {
        String itemName = "testItem";
        int itemId = 34;

        when(itemDefinition.getId()).thenReturn(itemId);
        when(inventoryService.getItemDefinition(itemName))
            .thenReturn(Optional.of(itemDefinition));
        when(inventoryService.removeItem(receiverUser, itemId, 1))
            .thenReturn(false);

        mockReceivingUser(receiver, receiverUser);

        mockServiceCreateFormatting(receiverUser,
            ItemMessageType.ITEM_GIVE_NOT_ENOUGH_OWNED);

        mockMessageFormats(ItemMessageType.ITEM_GIVE_NOT_ENOUGH_OWNED,
            "%(itemName):%(itemId):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.USER, "user", receiver),
            optionMapping(OptionType.STRING, "item", itemName));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(itemName + ":" + itemId + ":1"));
    }

    @Test
    void testRemoveUnknownItem(@Mock BlankUser user) {
        String itemName = "testItem";

        when(inventoryService.getItemDefinition(itemName))
            .thenReturn(Optional.empty());

        mockServiceCreateFormatting(user, ItemMessageType.ITEM_NOT_EXISTS);

        mockMessageFormats(ItemMessageType.ITEM_NOT_EXISTS, "%(itemName)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.USER, "user", null),
            optionMapping(OptionType.STRING, "item", itemName));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(
                embedHasDescription(itemName));

        verify(inventoryService, never()).giveItem(any(), anyInt(), anyInt());
    }

    @Test
    void testAutocomplete(@Mock BlankUser user) {
        String itemName = "testIt";

        List<Command.Choice> list = new LinkedList<>();

        CommandAutoCompleteInteractionEvent event = mockAutocompleteEvent(user,
            optionMapping(OptionType.STRING, "item", itemName));

        when(inventoryService
            .autoCompleteItems(itemName.toLowerCase()))
                .thenReturn(list);

        assertThat(callAutocomplete(event)).isEqualTo(list);
    }

    @Test
    void testAutocompleteUser(@Mock BlankUser user, @Mock Member member,
        @Mock BlankUser receiver) {
        String itemName = "testIt";

        List<Command.Choice> list = new LinkedList<>();

        CommandAutoCompleteInteractionEvent event = mockAutocompleteEvent(user,
            optionMapping(OptionType.STRING, "item", itemName),
            optionMapping(OptionType.USER, "user", member));

        mockReceivingUser(member, receiver);

        when(inventoryService
            .autoCompleteUserItems(receiver, itemName.toLowerCase()))
                .thenReturn(list);

        assertThat(callAutocomplete(event)).isEqualTo(list);
    }

    private void mockReceivingUser(Member receiver, BlankUser receiverUser) {
        lenient()
            .when(blankUserService
                .addUserDetailsFormattingData(any(), eq(receiverUser),
                    eq(GenericFormatDataKey.RECEIVING_USER),
                    eq(GenericFormatDataKey.RECEIVING_USER_MENTION)))
            .then(invocation -> invocation
                .getArgument(0,
                    FormattingData.FormattingDataBuilder.class));

        lenient()
            .when(blankUserService.getUser(receiver))
            .thenReturn(receiverUser);
        lenient()
            .when(blankUserService.getUser(any(OptionMapping.class)))
            .then(invocation -> {
                if (invocation
                    .getArgument(0, OptionMapping.class)
                    .getAsMember()
                    .equals(receiver)) {
                    return receiverUser;
                }
                return null;
            });
    }
}
