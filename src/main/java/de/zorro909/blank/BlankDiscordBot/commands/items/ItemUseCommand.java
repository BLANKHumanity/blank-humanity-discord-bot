package de.zorro909.blank.BlankDiscordBot.commands.items;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class ItemUseCommand extends AbstractCommand {

    public ItemUseCommand() {
	super("use");
    }

    @Autowired
    private InventoryService inventoryService;

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

	OptionMapping item = event.getOption("item");

	int amount = Optional
		.ofNullable(event.getOption("amount"))
		.map(OptionMapping::getAsLong)
		.orElse(1l)
		.intValue();

	inventoryService
		.useItem(user, item.getAsString(), amount,
			embeds -> reply(event, embeds));
    }

}
