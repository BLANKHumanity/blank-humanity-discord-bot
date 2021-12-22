package de.zorro909.blank.BlankDiscordBot.commands.items;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class ItemUseCommand extends AbstractCommand {

    @Autowired
    private InventoryService inventoryService;

    @Override
    protected CommandData createCommandData() {
	CommandData commandData = new CommandData("use", "Uses a Item");
	commandData.addOption(OptionType.STRING, "item", "Item to use", true);
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = blankUserService.getUser(event);

	OptionMapping item = event.getOption("item");

	inventoryService
		.useItem(user, item.getAsString(),
			embeds -> reply(event, embeds));
    }

}
