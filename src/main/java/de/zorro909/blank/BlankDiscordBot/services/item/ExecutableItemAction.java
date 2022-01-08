package de.zorro909.blank.BlankDiscordBot.services.item;

import java.util.function.Consumer;

import de.zorro909.blank.BlankDiscordBot.entities.item.Item;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;

public interface ExecutableItemAction {

    public ItemActionStatus executeAction(BlankUser user, Item item,
	    int amount, Consumer<FormattingData> reply);

}
