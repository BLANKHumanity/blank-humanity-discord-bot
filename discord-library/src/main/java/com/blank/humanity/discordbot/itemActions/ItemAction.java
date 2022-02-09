package com.blank.humanity.discordbot.itemActions;

import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

public interface ItemAction {

    public Class<? extends ExecutableItemAction> getExecutableItemAction();
    
}
