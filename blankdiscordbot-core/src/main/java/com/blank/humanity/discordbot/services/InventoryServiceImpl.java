package com.blank.humanity.discordbot.services;

import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.database.ItemDao;
import com.blank.humanity.discordbot.itemActions.ItemActionImpl;

import de.zorro909.blank.BlankDiscordBot.config.items.ItemConfiguration;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.entities.item.Item;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemAction;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.services.BlankUserService;
import de.zorro909.blank.BlankDiscordBot.services.InventoryService;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import de.zorro909.blank.BlankDiscordBot.utils.item.ExecutableItemAction;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemConfiguration itemConfiguration;

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
    public ItemActionStatus useItem(BlankUser user, String useName, int amount,
	    Consumer<FormattingData> reply) {
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
		.map(ItemActionImpl::valueOf)
		.map(ItemAction::getExecutableItemAction)
		.map(applicationContext::getBean);

	Optional<ItemDefinition> itemDefinition = item
		.map(Item::getItemId)
		.flatMap(this::getItemDefinition);

	if (item.isEmpty() || itemDefinition.isEmpty()) {
	    FormattingData data = blankUserService
		    .createFormattingData(user, ItemMessageType.ITEM_NOT_EXISTS)
		    .dataPairing(ItemFormatDataKey.ITEM_NAME, useName)
		    .build();
	    reply.accept(data);
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}

	if (action.isEmpty()) {
	    FormattingData data = blankUserService
		    .createFormattingData(user,
			    ItemMessageType.ITEM_USE_ACTION_UNDEFINED)
		    .dataPairing(ItemFormatDataKey.ITEM_ID,
			    item.get().getItemId())
		    .dataPairing(ItemFormatDataKey.ITEM_NAME, useName)
		    .build();
	    reply.accept(data);
	    return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
	}

	if (!removeItem(user, item.get().getItemId(), amount)) {
	    FormattingData data = blankUserService
		    .createFormattingData(user,
			    ItemMessageType.ITEM_USE_NOT_OWNED)
		    .dataPairing(ItemFormatDataKey.ITEM_ID,
			    item.get().getItemId())
		    .dataPairing(ItemFormatDataKey.ITEM_NAME, useName)
		    .dataPairing(ItemFormatDataKey.ITEM_AMOUNT, amount)
		    .build();
	    reply.accept(data);
	    return ItemActionStatus.ITEM_NOT_OWNED;
	}

	ItemActionStatus status = action
		.get()
		.executeAction(user, itemDefinition.get(), amount, reply);

	if (status != ItemActionStatus.SUCCESS) {
	    // On Error give Item back
	    giveItem(user, item.get().getItemId(), amount);
	}
	return status;
    }

    public Optional<ItemDefinition> getItemDefinition(int itemId) {
	return itemConfiguration.getItemDefinition(itemId);
    }

    public Optional<ItemDefinition> getItemDefinition(String itemName) {
	return itemConfiguration
		.getDefinitions()
		.stream()
		.filter(item -> item.getUseName().equalsIgnoreCase(itemName))
		.findAny();
    }

}
