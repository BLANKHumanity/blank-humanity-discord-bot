package com.blank.humanity.discordbot.utils.item;

import java.util.function.Consumer;

import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.itemActions.ItemActionStatus;
import com.blank.humanity.discordbot.utils.FormattingData;

public interface ExecutableItemAction {

    public ItemActionStatus executeAction(BlankUser user, ItemDefinition item,
	    int amount, Consumer<FormattingData> reply);

}
