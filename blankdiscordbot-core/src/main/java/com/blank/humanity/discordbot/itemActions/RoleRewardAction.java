package com.blank.humanity.discordbot.itemActions;

import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.itemActions.messages.ItemActionFormatDataKey;
import com.blank.humanity.discordbot.itemActions.messages.ItemActionMessageType;

import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericFormatDataKey;
import de.zorro909.blank.BlankDiscordBot.config.messages.GenericMessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.item.ExecutableItemAction;
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
    public ItemActionStatus executeAction(BlankUser user, ItemDefinition item,
	    int amount, Consumer<FormattingData> reply) {
	if (amount > 1) {
	    reply
		    .accept(blankUserService
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
		.of(item.getActionArguments())
		.map(map -> map.get("roleId"))
		.map(String::valueOf)
		.map(Long::parseLong);

	Optional<Role> role = roleId.map(guild::getRoleById);

	if (roleId.isEmpty()) {
	    reply
		    .accept(error(user, "Item '" + item.getId()
			    + "' has no associated roleId to reward!"));
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}
	if (role.isEmpty()) {
	    reply
		    .accept(error(user, "Role Id '" + roleId.get()
			    + "' could not be found!"));
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}

	if (discordMember.getRoles().contains(role.get())) {
	    reply
		    .accept(blankUserService
			    .createFormattingData(user,
				    ItemActionMessageType.ROLE_REWARD_ALREADY_CLAIMED)
			    .dataPairing(ItemActionFormatDataKey.ROLE,
				    role.get().getName())
			    .build());
	    return ItemActionStatus.GENERIC_ERROR;
	} else {
	    guild.addRoleToMember(discordMember, role.get()).complete();
	    reply
		    .accept(blankUserService
			    .createFormattingData(user,
				    ItemActionMessageType.ROLE_REWARD_CLAIMED)
			    .dataPairing(ItemActionFormatDataKey.ROLE,
				    role.get().getName())
			    .build());
	    return ItemActionStatus.SUCCESS;
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
