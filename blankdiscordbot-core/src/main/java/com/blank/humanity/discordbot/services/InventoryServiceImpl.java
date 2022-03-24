package com.blank.humanity.discordbot.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.database.ItemDao;
import com.blank.humanity.discordbot.entities.item.Item;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemConfiguration itemConfiguration;

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
