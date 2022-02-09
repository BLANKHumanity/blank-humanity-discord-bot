package com.blank.humanity.discordbot.itemActions;

import javax.validation.constraints.NotNull;

import de.zorro909.blank.BlankDiscordBot.itemActions.ItemAction;
import de.zorro909.blank.BlankDiscordBot.utils.item.ExecutableItemAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ItemActionImpl implements ItemAction {
    ROLE_REWARD(RoleRewardAction.class),
    MESSAGE_ACTION(MessageAction.class);
    
    @NotNull
    private Class<? extends ExecutableItemAction> executableItemAction;
    
}
