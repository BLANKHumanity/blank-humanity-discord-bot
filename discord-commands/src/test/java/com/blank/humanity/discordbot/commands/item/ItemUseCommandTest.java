package com.blank.humanity.discordbot.commands.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.items.ItemUseCommand;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Disabled
class ItemUseCommandTest extends CommandUnitTest<ItemUseCommand> {

    @Mock
    private InventoryService inventoryService;

    protected ItemUseCommandTest() {
        super(ItemUseCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions())
            .hasSize(2)
            .anyMatch(hasOption("item", OptionType.STRING, true, true))
            .anyMatch(hasOption("amount", OptionType.INTEGER, false));
    }

    @BeforeEach
    void setupInventoryService() {
        commandMock.setInventoryService(inventoryService);
    }

    @Test
    void testSingleItemUse(@Mock BlankUser user) {
        String itemName = "testITEM";
        FormattingData emptyMessage = FormattingData
            .builder()
            .messageType(GenericMessageType.ERROR_MESSAGE)
            .build();

        mockMessageFormats(GenericMessageType.ERROR_MESSAGE, "test");

//        when(inventoryService.useItem(any(), any(), anyInt(), any()))
//            .then(invocation -> issueMockReply(invocation, emptyMessage));

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "item", itemName));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription("test"));

//        verify(inventoryService)
//            .useItem(eq(user), eq(itemName), eq(1), any());
    }

    @Test
    void testMultipleItemsUse(@Mock BlankUser user) {
        String itemName = "testITEM";
        int amount = 4;
        FormattingData emptyMessage = FormattingData
            .builder()
            .messageType(GenericMessageType.ERROR_MESSAGE)
            .build();

        mockMessageFormats(GenericMessageType.ERROR_MESSAGE, "test");

//        when(inventoryService.useItem(any(), any(), anyInt(), any()))
//            .then(invocation -> issueMockReply(invocation, emptyMessage));

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "item", itemName),
            optionMapping(OptionType.INTEGER, "amount", amount));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription("test"));

//        verify(inventoryService)
//            .useItem(eq(user), eq(itemName), eq(4), any());
    }

    @Test
    void testEmptyAutocomplete(@Mock BlankUser user) {
        List<Command.Choice> choices = List
            .of(new Command.Choice("testChoice", 312));

        when(inventoryService.autoCompleteUserItems(user, ""))
            .thenReturn(choices);

        CommandAutoCompleteInteractionEvent event = mockAutocompleteEvent(user);

        assertThat(callAutocomplete(event)).isEqualTo(choices);
    }

    @Test
    void testAutocomplete(@Mock BlankUser user) {
        List<Command.Choice> choices = List
            .of(new Command.Choice("weirdOne", 312));

        when(inventoryService.autoCompleteUserItems(user, "weird"))
            .thenReturn(choices);

        CommandAutoCompleteInteractionEvent event = mockAutocompleteEvent(user,
            optionMapping(OptionType.STRING, "item", "weird"));

        assertThat(callAutocomplete(event)).isEqualTo(choices);
    }

    @SuppressWarnings("unchecked")
    private Object issueMockReply(InvocationOnMock invocation,
        FormattingData formattingData) {
        invocation
            .getArgument(3, Consumer.class)
            .accept(formattingData);
        return null;
    }

}
