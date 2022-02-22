package com.blank.humanity.discordbot.commands.items;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.InventoryService;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class ItemUseCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "use";
    }

    private static final String ITEM = "item";
    private static final String AMOUNT = "amount";

    @Autowired
    private InventoryService inventoryService;

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

        OptionMapping item = event.getOption(ITEM);

        int amount = Optional
            .ofNullable(event.getOption(AMOUNT))
            .map(OptionMapping::getAsLong)
            .orElse(1l)
            .intValue();

        inventoryService
            .useItem(user, item.getAsString(), amount,
                embeds -> reply(event, embeds));
    }

}
