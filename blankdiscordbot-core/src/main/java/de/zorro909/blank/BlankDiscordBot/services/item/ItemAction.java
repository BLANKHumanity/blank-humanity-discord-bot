package de.zorro909.blank.BlankDiscordBot.services.item;

import javax.validation.constraints.NotNull;

import de.zorro909.blank.BlankDiscordBot.itemActions.RoleRewardAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ItemAction {
    ROLE_REWARD(RoleRewardAction.class);
    
    @NotNull
    private Class<? extends ExecutableItemAction> executableItemAction;
    
}
