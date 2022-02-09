package de.zorro909.blank.BlankDiscordBot.utils.item;

import java.util.function.Consumer;

import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;

public interface ExecutableItemAction {

    public ItemActionStatus executeAction(BlankUser user, ItemDefinition item,
	    int amount, Consumer<FormattingData> reply);

}
