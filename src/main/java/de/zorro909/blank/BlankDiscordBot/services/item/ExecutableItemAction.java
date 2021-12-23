package de.zorro909.blank.BlankDiscordBot.services.item;

import java.util.function.Consumer;

import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.Item;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;

public interface ExecutableItemAction {

    public ItemActionStatus executeAction(BlankUser user, Item item,
	    Consumer<FormattingData> reply);

}
