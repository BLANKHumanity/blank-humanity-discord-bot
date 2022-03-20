package com.blank.humanity.discordbot.commands.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import com.blank.humanity.discordbot.commands.items.GiftItemCommand;
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

class GiftItemCommandTest extends CommandUnitTest<GiftItemCommand> {

    @Mock
    private InventoryService inventoryService;

    protected GiftItemCommandTest() {
        super(GiftItemCommand.class);
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
    void testGiftSingleItem(@Mock BlankUser user, @Mock Member receiver,
        @Mock BlankUser receiverUser, @Mock ItemDefinition itemDefinition) {
        String itemName = "testItem";
        int itemId = 34;

        when(itemDefinition.getId()).thenReturn(itemId);
        when(itemDefinition.getName()).thenReturn(itemName);
        when(inventoryService.getItemDefinition(itemName))
            .thenReturn(Optional.of(itemDefinition));

        mockReceivingUser(receiver, receiverUser);

        mockServiceCreateFormatting(user, ItemMessageType.ITEM_GIVE_SUCCESS);

        mockMessageFormats(ItemMessageType.ITEM_GIVE_SUCCESS,
            "%(itemName):%(itemId):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.USER, "user", receiver),
            optionMapping(OptionType.STRING, "item", itemName));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(itemName + ":" + itemId + ":1"));

        verify(inventoryService)
            .giveItem(receiverUser, itemDefinition.getId(), 1);
    }

    @Test
    void testGiftMultipleItems(@Mock BlankUser user, @Mock Member receiver,
        @Mock BlankUser receiverUser, @Mock ItemDefinition itemDefinition) {
        String itemName = "testItem";
        int itemId = 34;
        int amount = 3;

        when(itemDefinition.getId()).thenReturn(itemId);
        when(itemDefinition.getName()).thenReturn(itemName);
        when(inventoryService.getItemDefinition(itemName))
            .thenReturn(Optional.of(itemDefinition));

        mockReceivingUser(receiver, receiverUser);

        mockServiceCreateFormatting(user, ItemMessageType.ITEM_GIVE_SUCCESS);

        mockMessageFormats(ItemMessageType.ITEM_GIVE_SUCCESS,
            "%(itemName):%(itemId):%(itemAmount)");

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.USER, "user", receiver),
            optionMapping(OptionType.STRING, "item", itemName),
            optionMapping(OptionType.INTEGER, "amount", amount));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(
                embedHasDescription(itemName + ":" + itemId + ":" + amount));

        verify(inventoryService)
            .giveItem(receiverUser, itemDefinition.getId(), amount);
    }

    @Test
    void testGiftUnknownItem(@Mock BlankUser user) {
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
    void testAutocomplete() {
        String itemName = "testIt";

        List<Command.Choice> list = new LinkedList<>();

        CommandAutoCompleteInteractionEvent event = mockAutocompleteEvent(
            optionMapping(OptionType.STRING, "item", itemName));

        when(inventoryService.autoCompleteItems(itemName.toLowerCase())).thenReturn(list);

        assertThat(callAutocomplete(event)).isEqualTo(list);
    }

    private void mockReceivingUser(Member receiver, BlankUser receiverUser) {
        when(blankUserService
            .addUserDetailsFormattingData(any(), eq(receiverUser),
                eq(GenericFormatDataKey.RECEIVING_USER),
                eq(GenericFormatDataKey.RECEIVING_USER_MENTION)))
                    .then(invocation -> invocation
                        .getArgument(0,
                            FormattingData.FormattingDataBuilder.class));

        when(blankUserService.getUser(any(OptionMapping.class)))
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
