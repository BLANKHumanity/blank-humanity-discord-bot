package com.blank.humanity.discordbot.item.actions;

import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

public interface ItemAction {

    public Class<? extends ExecutableItemAction> getExecutableItemAction();
    
}
