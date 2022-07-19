package com.blank.humanity.discordbot.commands.items;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.aop.Argument;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.services.InventoryService;

import lombok.Setter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Argument(name = "user", type = OptionType.USER)
@Argument(name = "item", autocomplete = true)
@Argument(name = "amount", type = OptionType.INTEGER, required = false, minValue = 1)
public abstract class ItemInteractionCommand extends AbstractCommand {

    protected static final String USER = "user";
    protected static final String ITEM = "item";
    protected static final String AMOUNT = "amount";

    @Setter(onMethod = @__({ @Autowired }))
    protected InventoryService inventoryService;

}
