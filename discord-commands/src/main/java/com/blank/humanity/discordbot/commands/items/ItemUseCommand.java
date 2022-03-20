package com.blank.humanity.discordbot.commands.items;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.services.InventoryService;

import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class ItemUseCommand extends AbstractCommand {

    private static final String ITEM = "item";
    private static final String AMOUNT = "amount";

    @Setter(onMethod = @__({ @Autowired }))
    private InventoryService inventoryService;

    @Override
    public String getCommandName() {
        return "use";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.STRING, ITEM,
                definition.getOptionDescription(ITEM),
                true, true);
        OptionData amount = new OptionData(OptionType.INTEGER, AMOUNT,
            definition.getOptionDescription(AMOUNT));
        amount.setMinValue(1);
        commandData.addOptions(amount);
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        String item = event.getOption(ITEM, OptionMapping::getAsString);

        int amount = event
            .getOption(AMOUNT, 1l, OptionMapping::getAsLong)
            .intValue();

        inventoryService
            .useItem(getUser(), item, amount, this::reply);
    }

    @Override
    protected Collection<Command.Choice> onAutoComplete(
        CommandAutoCompleteInteractionEvent event) {
        String itemName = event
            .getOption(ITEM, () -> "", OptionMapping::getAsString)
            .toLowerCase();

        return inventoryService
            .autoCompleteUserItems(getUser(), itemName);
    }

}
