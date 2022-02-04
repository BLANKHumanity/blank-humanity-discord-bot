package de.zorro909.blank.BlankDiscordBot.services;

import java.util.Optional;
import java.util.function.Consumer;

import de.zorro909.blank.BlankDiscordBot.config.items.ItemDefinition;
import de.zorro909.blank.BlankDiscordBot.entities.item.Item;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.itemActions.ItemActionStatus;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;

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

}
