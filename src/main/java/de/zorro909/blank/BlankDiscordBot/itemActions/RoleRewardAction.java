package de.zorro909.blank.BlankDiscordBot.itemActions;

import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.Item;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import de.zorro909.blank.BlankDiscordBot.services.item.ExecutableItemAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

@Component
public class RoleRewardAction implements ExecutableItemAction {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private JDA jda;

    @Override
    public ItemActionStatus executeAction(BlankUser user, Item item,
	    Consumer<MessageEmbed[]> reply) {
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
		    .accept(error("Item '" + item.getItemId()
			    + "' has no associated roleId to reward!"));
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}
	if (role.isEmpty()) {
	    reply
		    .accept(error("Role Id '" + roleId.get()
			    + "' could not be found!"));
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}

	if (discordMember.getRoles().contains(role.get())) {
	    MessageEmbed embed = new EmbedBuilder()
		    .setTitle("Role Already claimed")
		    .setDescription(discordMember.getAsMention()
			    + " already has the Role '" + role.get().getName()
			    + "'")
		    .build();
	    reply.accept(new MessageEmbed[] { embed });
	    return ItemActionStatus.GENERIC_ERROR;
	} else {
	    guild.addRoleToMember(discordMember, role.get()).complete();
	    MessageEmbed embed = new EmbedBuilder()
		    .setTitle("Role claimed!")
		    .setDescription(
			    discordMember.getAsMention() + " claimed the Role '"
				    + role.get().getName() + "'")
		    .build();
	    reply.accept(new MessageEmbed[] { embed });
	    return ItemActionStatus.SUCCESS;
	}
    }

    private MessageEmbed[] error(String description) {
	MessageEmbed embed = new EmbedBuilder()
		.setDescription(
			description + "\nPlease contact an administrator")
		.build();
	return new MessageEmbed[] { embed };
    }

}
