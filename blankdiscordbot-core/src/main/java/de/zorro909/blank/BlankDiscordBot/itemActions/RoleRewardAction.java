package de.zorro909.blank.BlankDiscordBot.itemActions;

import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.item.Item;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import de.zorro909.blank.BlankDiscordBot.services.item.ExecutableItemAction;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

@Component
public class RoleRewardAction implements ExecutableItemAction {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private JDA jda;

    @Override
    public ItemActionStatus executeAction(BlankUser user, Item item, int amount,
	    Consumer<FormattingData> reply) {
	if (amount > 1) {
	    ItemDefinition definition = inventoryService
		    .getItemDefinition(item.getItemId())
		    .get();
	    reply
		    .accept(blankUserService
			    .createFormattingData(user,
				    MessageType.ITEM_USE_ONLY_SINGLE_ITEM)
			    .dataPairing(FormatDataKey.ITEM_ID,
				    item.getItemId())
			    .dataPairing(FormatDataKey.ITEM_NAME,
				    definition.getName())
			    .build());
	    return ItemActionStatus.GENERIC_ERROR;
	}

	Guild guild = jda.getGuildById(user.getGuildId());
	Member discordMember = guild
		.retrieveMemberById(user.getDiscordId())
		.complete();

	Optional<Long> roleId = inventoryService
		.getItemDefinition(item.getItemId())
		.map(ItemDefinition::getActionArguments)
		.map(map -> Long.valueOf(String.valueOf(map.get("roleId"))));

	Optional<Role> role = roleId.map((id) -> guild.getRoleById(id));

	if (roleId.isEmpty()) {
	    reply
		    .accept(error(user, "Item '" + item.getItemId()
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
				    MessageType.ROLE_REWARD_ALREADY_CLAIMED)
			    .dataPairing(FormatDataKey.ROLE,
				    role.get().getName())
			    .build());
	    return ItemActionStatus.GENERIC_ERROR;
	} else {
	    guild.addRoleToMember(discordMember, role.get()).complete();
	    reply
		    .accept(blankUserService
			    .createFormattingData(user,
				    MessageType.ROLE_REWARD_CLAIMED)
			    .dataPairing(FormatDataKey.ROLE,
				    role.get().getName())
			    .build());
	    return ItemActionStatus.SUCCESS;
	}
    }

    private FormattingData error(BlankUser user, String errorDescription) {
	return blankUserService
		.createFormattingData(user, MessageType.ERROR_MESSAGE)
		.dataPairing(FormatDataKey.ERROR_MESSAGE, errorDescription)
		.build();
    }

}
