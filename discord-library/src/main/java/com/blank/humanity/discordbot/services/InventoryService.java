package com.blank.humanity.discordbot.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.entities.item.Item;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.item.actions.ItemActionStatus;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.interactions.commands.Command;

public interface InventoryService {

    public Optional<Item> getItem(BlankUser user, int itemId);

    public void giveItem(BlankUser user, int itemId, int amount);

    public void giveItem(BlankUser user, int itemId);

    public boolean removeItem(BlankUser user, int itemId, int amount);

    public boolean removeItem(BlankUser user, int itemId);

    public ItemActionStatus useItem(BlankUser user, String useName, int amount,
        Consumer<FormattingData> reply);

    public Optional<ItemDefinition> getItemDefinition(int itemId);

    public Optional<ItemDefinition> getItemDefinition(String itemName);

    public List<ItemDefinition> searchItems(String partialItemName);

    public Collection<Command.Choice> autoCompleteUserItems(BlankUser user,
        String partialItemName);

    public Collection<Command.Choice> autoCompleteItems(String partialItemName);

    interface ItemGetter {
        public String get(ItemDefinition definition);
    }

}
