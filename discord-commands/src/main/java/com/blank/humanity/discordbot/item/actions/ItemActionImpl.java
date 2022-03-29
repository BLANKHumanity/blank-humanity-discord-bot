package com.blank.humanity.discordbot.item.actions;

import javax.validation.constraints.NotNull;

import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ItemActionImpl implements ItemAction {
    ROLE_REWARD(RoleRewardAction.class),
    MESSAGE_SEND(MessageSendAction.class),
    MESSAGE_REPLY(MessageReplyAction.class),
    REQUIRED_AMOUNT(RequiredAmountAction.class),
    RANDOM_NUMBER(RandomNumberAction.class),
    RANDOM_SELECTION(RandomSelectionAction.class),
    ITEM_REWARD(ItemRewardAction.class);

    @NotNull
    private Class<? extends ExecutableItemAction> executableItemAction;

}
