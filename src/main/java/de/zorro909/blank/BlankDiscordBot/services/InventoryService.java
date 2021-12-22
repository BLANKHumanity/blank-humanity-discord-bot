package de.zorro909.blank.BlankDiscordBot.services;

import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.zorro909.blank.BlankDiscordBot.config.MessagesConfig;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemConfiguration;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.database.ItemDao;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.Item;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.services.item.ExecutableItemAction;
import de.zorro909.blank.BlankDiscordBot.services.item.ItemAction;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.NamedFormatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class InventoryService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Autowired
    private MessagesConfig messagesConfig;

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private ApplicationContext applicationContext;

    @Transactional
    public Optional<Item> getItem(BlankUser user, int itemId) {
	return user
		.getItems()
		.stream()
		.filter((item) -> item.getItemId() == itemId)
		.findAny();
    }

    @Transactional
    public void giveItem(BlankUser user, int itemId, int amount) {
	Optional<Item> item = getItem(user, itemId);
	if (item.isPresent()) {
	    Item existingItem = item.get();
	    existingItem.setAmount(existingItem.getAmount() + amount);
	} else {
	    Item newItem = new Item(itemId, amount, user);
	    newItem = itemDao.save(newItem);
	}
    }

    @Transactional
    public void giveItem(BlankUser user, int itemId) {
	giveItem(user, itemId, 1);
    }

    @Transactional
    public boolean removeItem(BlankUser user, int itemId, int amount) {
	Optional<Item> item = getItem(user, itemId);
	Optional<Integer> oldAmount = item.map(Item::getAmount);

	if (oldAmount.orElse(0) < amount) {
	    return false;
	}
	Item inventoryItem = item.get();
	if (inventoryItem.getAmount() == amount) {
	    user.getItems().remove(inventoryItem);
	    itemDao.delete(inventoryItem);
	} else {
	    inventoryItem.setAmount(inventoryItem.getAmount() - amount);
	}
	return true;
    }

    @Transactional
    public boolean removeItem(BlankUser user, int itemId) {
	return removeItem(user, itemId, 1);
    }

    @Transactional
    public ItemActionStatus useItem(BlankUser user, String useName,
	    Consumer<MessageEmbed[]> reply) {
	Optional<Item> item = itemConfiguration
		.getDefinitions()
		.stream()
		.filter(itemDefinition -> itemDefinition
			.getUseName()
			.equalsIgnoreCase(useName))
		.findFirst()
		.map(ItemDefinition::getId)
		.flatMap(id -> getItem(user, id));

	Optional<ExecutableItemAction> action = item
		.map(Item::getItemId)
		.flatMap(itemConfiguration::getItemDefinition)
		.map(ItemDefinition::getAction)
		.map(ItemAction::getExecutableItemAction)
		.map(applicationContext::getBean);

	if (item.isEmpty()) {
	    FormattingData data = blankUserService
		    .createFormattingData(user)
		    .dataPairing(FormatDataKey.ITEM_NAME, useName)
		    .build();
	    MessageEmbed embed = new EmbedBuilder()
		    .setDescription(NamedFormatter
			    .namedFormat(messagesConfig.ITEM_NOT_EXISTS,
				    data.getDataPairings()))
		    .build();
	    reply.accept(new MessageEmbed[] { embed });
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}

	if (action.isEmpty()) {
	    FormattingData data = blankUserService
		    .createFormattingData(user)
		    .dataPairing(FormatDataKey.ITEM_ID, item.get().getItemId())
		    .dataPairing(FormatDataKey.ITEM_NAME, useName)
		    .build();
	    MessageEmbed embed = new EmbedBuilder()
		    .setDescription(NamedFormatter
			    .namedFormat(
				    messagesConfig.ITEM_USE_ACTION_UNDEFINED,
				    data.getDataPairings()))
		    .build();
	    reply.accept(new MessageEmbed[] { embed });
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}

	if (!removeItem(user, item.get().getItemId())) {
	    FormattingData data = blankUserService
		    .createFormattingData(user)
		    .dataPairing(FormatDataKey.ITEM_ID, item.get().getItemId())
		    .dataPairing(FormatDataKey.ITEM_NAME, useName)
		    .build();
	    MessageEmbed embed = new EmbedBuilder()
		    .setDescription(NamedFormatter
			    .namedFormat(messagesConfig.ITEM_USE_NOT_OWNED,
				    data.getDataPairings()))
		    .build();
	    reply.accept(new MessageEmbed[] { embed });
	    return ItemActionStatus.ITEM_NOT_OWNED;
	}

	ItemActionStatus status = action
		.get()
		.executeAction(user, item.get(), reply);

	if (status != ItemActionStatus.SUCCESS) {
	    // On Error give Item back
	    giveItem(user, item.get().getItemId());
	}
	return status;
    }

    public Optional<ItemDefinition> getItemDefinition(int itemId) {
	return itemConfiguration.getItemDefinition(itemId);
    }

}
