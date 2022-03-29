package com.blank.humanity.discordbot.item.actions;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.item.actions.messages.ItemActionFormatDataKey;
import com.blank.humanity.discordbot.item.actions.messages.ItemActionMessageType;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Component
public class RoleRewardAction implements ExecutableItemAction {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private JDA jda;

    @Override
    public ItemActionStatus executeAction(BlankUser user,
        ItemActionState itemActionState) {
        ItemDefinition item = itemActionState.getItemDefinition();

        if (itemActionState.getAmount() > 1) {
            itemActionState
                .reply(blankUserService
                    .createFormattingData(user,
                        ItemActionMessageType.ITEM_USE_ONLY_SINGLE_ITEM)
                    .dataPairing(ItemFormatDataKey.ITEM_ID,
                        item.getId())
                    .dataPairing(ItemFormatDataKey.ITEM_NAME,
                        item.getName())
                    .build());
            return ItemActionStatus.GENERIC_ERROR;
        }

        Guild guild = jda.getGuildById(user.getGuildId());
        Member discordMember = guild
            .retrieveMemberById(user.getDiscordId())
            .complete();

        Optional<Long> roleId = Optional
            .ofNullable(
                itemActionState.getProperty("roleId", Long::parseLong, null));

        Optional<Role> role = roleId.map(guild::getRoleById);

        if (roleId.isEmpty()) {
            itemActionState
                .reply(error(user, "Item '" + item.getId()
                    + "' has no associated roleId to reward!"));
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }
        if (role.isEmpty()) {
            itemActionState
                .reply(error(user, "Role Id '" + roleId.get()
                    + "' could not be found!"));
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        if (discordMember.getRoles().contains(role.get())) {
            itemActionState
                .reply(blankUserService
                    .createFormattingData(user,
                        ItemActionMessageType.ROLE_REWARD_ALREADY_CLAIMED)
                    .dataPairing(ItemActionFormatDataKey.ROLE,
                        role.get().getName())
                    .build());
            return ItemActionStatus.GENERIC_ERROR;
        } else {
            guild.addRoleToMember(discordMember, role.get()).complete();
            itemActionState
                .reply(blankUserService
                    .createFormattingData(user,
                        ItemActionMessageType.ROLE_REWARD_CLAIMED)
                    .dataPairing(ItemActionFormatDataKey.ROLE,
                        role.get().getName())
                    .build());
            return itemActionState.doNext(user);
        }
    }

    private FormattingData error(BlankUser user, String errorDescription) {
        return blankUserService
            .createFormattingData(user, GenericMessageType.ERROR_MESSAGE)
            .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
                errorDescription)
            .build();
    }

}
