package de.zorro909.blank.BlankDiscordBot.itemActions;

import javax.validation.constraints.NotNull;

import de.zorro909.blank.BlankDiscordBot.utils.item.ExecutableItemAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ItemActionImpl implements ItemAction {
    ROLE_REWARD(RoleRewardAction.class);
    
    @NotNull
    private Class<? extends ExecutableItemAction> executableItemAction;
    
}
