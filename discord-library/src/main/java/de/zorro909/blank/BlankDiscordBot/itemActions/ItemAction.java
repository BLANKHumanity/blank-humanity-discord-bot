package de.zorro909.blank.BlankDiscordBot.itemActions;

import de.zorro909.blank.BlankDiscordBot.utils.item.ExecutableItemAction;

public interface ItemAction {

    public Class<? extends ExecutableItemAction> getExecutableItemAction();
    
}
