package de.zorro909.blank.BlankDiscordBot.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.zorro909.blank.BlankDiscordBot.config.items.ItemShopConfig;
import de.zorro909.blank.BlankDiscordBot.config.items.ShopItem;
import de.zorro909.blank.BlankDiscordBot.database.ItemDao;
import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.services.item.ItemBuyStatus;

@Service
public class ShopService {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ItemShopConfig itemShopConfig;

    @Autowired
    private ItemDao itemDao;

    public long amountShopPages() {
	return (long) Math
		.ceil((double) itemShopConfig.getShopItems().size()
			/ (double) itemShopConfig.getItemsPerPage());
    }

    public List<ShopItem> getShopPage(@Valid @Min(1) int page) {
	return itemShopConfig
		.getShopItems()
		.stream()
		.sorted(Comparator.comparing(ShopItem::getId))
		.skip(itemShopConfig.getItemsPerPage() * (page - 1))
		.limit(itemShopConfig.getItemsPerPage())
		.collect(Collectors.toList());
    }

    public Optional<ShopItem> getShopItem(String buyName) {
	return itemShopConfig.getShopItem(buyName);
    }

    @Transactional
    public ItemBuyStatus buyItem(BlankUser user, ShopItem item) {
	if (user.getBalance() < item.getPrice()) {
	    return ItemBuyStatus.NOT_ENOUGH_MONEY;
	}

	if (getAvailableItemAmount(item) <= 0) {
	    return ItemBuyStatus.NO_AVAILABLE_SUPPLY;
	}

	blankUserService.decreaseUserBalance(user, item.getPrice());
	inventoryService.giveItem(user, item.getItemId());
	return ItemBuyStatus.SUCCESS;
    }

    public int getAvailableItemAmount(ShopItem item) {
	return item.getAmountAvailable()
		- itemDao.sumOfAllExistingItems(item.getItemId());
    }

}
