package com.blank.humanity.discordbot.item.actions;

import javax.validation.constraints.NotNull;

import com.blank.humanity.discordbot.itemActions.ItemAction;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

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
