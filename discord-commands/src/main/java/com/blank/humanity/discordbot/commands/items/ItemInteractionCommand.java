package com.blank.humanity.discordbot.commands.items;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.services.InventoryService;

import lombok.Setter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class ItemInteractionCommand extends AbstractCommand {

    protected static final String USER = "user";
    protected static final String ITEM = "item";
    protected static final String AMOUNT = "amount";

    @Setter(onMethod = @__({ @Autowired }))
    protected InventoryService inventoryService;

    @Override
    public CommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.USER, USER,
                definition.getOptionDescription(USER),
                true);
        commandData
            .addOption(OptionType.STRING, ITEM,
                definition.getOptionDescription(ITEM),
                true, true);
        OptionData data = new OptionData(OptionType.INTEGER, AMOUNT,
            definition.getOptionDescription(AMOUNT), false);
        data.setMinValue(1);
        commandData.addOptions(data);

        return commandData;
    }

}
