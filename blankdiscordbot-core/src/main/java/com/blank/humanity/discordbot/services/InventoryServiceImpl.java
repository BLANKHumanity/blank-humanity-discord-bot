package com.blank.humanity.discordbot.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blank.humanity.discordbot.commands.items.messages.ItemFormatDataKey;
import com.blank.humanity.discordbot.commands.items.messages.ItemMessageType;
import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.database.ItemDao;
import com.blank.humanity.discordbot.entities.item.Item;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.itemActions.ItemAction;
import com.blank.humanity.discordbot.itemActions.ItemActionImpl;
import com.blank.humanity.discordbot.itemActions.ItemActionStatus;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.item.ExecutableItemAction;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Slf4j
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
            .filter(item -> item.getItemId() == itemId)
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
            itemDao.save(newItem);
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

        if (oldAmount.orElse(0) < amount || item.isEmpty()) {
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
        Optional<ItemDefinition> itemDefinition = getItemDefinition(useName);

        Optional<ExecutableItemAction> action = itemDefinition
            .map(ItemDefinition::getAction)
            .map(ItemActionImpl::valueOf)
            .map(ItemAction::getExecutableItemAction)
            .map(applicationContext::getBean);

        if (itemDefinition.isEmpty()) {
            FormattingData data = blankUserService
                .createFormattingData(user, ItemMessageType.ITEM_NOT_EXISTS)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, useName)
                .build();
            reply.accept(data);
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        ItemDefinition resolvedItemDefinition = itemDefinition.get();
        int itemId = resolvedItemDefinition.getId();

        if (action.isEmpty()) {
            FormattingData data = blankUserService
                .createFormattingData(user,
                    ItemMessageType.ITEM_USE_ACTION_UNDEFINED)
                .dataPairing(ItemFormatDataKey.ITEM_ID, itemId)
                .dataPairing(ItemFormatDataKey.ITEM_NAME, useName)
                .build();
            reply.accept(data);
            return ItemActionStatus.ITEM_CONFIGURATION_ERROR;
        }

        if (!removeItem(user, itemId, amount)) {
            FormattingData data = blankUserService
                .createFormattingData(user,
                    ItemMessageType.ITEM_USE_NOT_OWNED)
                .dataPairing(ItemFormatDataKey.ITEM_ID, itemId)
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
            giveItem(user, itemId, amount);
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
            .filter(item -> item.getUseName() != null)
            .filter(item -> item.getUseName().equalsIgnoreCase(itemName))
            .findAny();
    }

    @Override
    public List<ItemDefinition> searchItems(String partialItemName) {
        String nameToMatch = partialItemName.toLowerCase().strip();
        return itemConfiguration
            .getDefinitions()
            .stream()
            .filter(item -> item.getUseName() != null)
            .filter(item -> item
                .getName()
                .toLowerCase()
                .contains(nameToMatch)
                || item.getUseName().toLowerCase().contains(nameToMatch))
            .toList();
    }

    @Override
    public Collection<Choice> autoCompleteUserItems(BlankUser user,
        String partialItemName) {

        List<Integer> userOwnedItems = user
            .getItems()
            .stream()
            .map(Item::getItemId)
            .toList();

        return itemConfiguration
            .getDefinitions()
            .stream()
            .filter(item -> item
                .getName()
                .toLowerCase()
                .contains(partialItemName.toLowerCase())
                || item
                    .getUseName()
                    .toLowerCase()
                    .contains(partialItemName.toLowerCase()))
            .filter(item -> userOwnedItems.contains(item.getId()))
            .map(item -> new Choice(item.getName(), item.getUseName()))
            .toList();
    }

    @Override
    public Collection<Choice> autoCompleteItems(String partialItemName) {
        return itemConfiguration
            .getDefinitions()
            .stream()
            .filter(item -> item
                .getName()
                .toLowerCase()
                .contains(partialItemName.toLowerCase())
                || item
                    .getUseName()
                    .toLowerCase()
                    .contains(partialItemName.toLowerCase()))
            .map(item -> new Choice(item.getName(), item.getUseName()))
            .toList();
    }

}
