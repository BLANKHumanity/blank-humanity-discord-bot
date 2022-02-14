package com.blank.humanity.discordbot.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.blank.humanity.discordbot.config.items.ItemShopConfig;
import com.blank.humanity.discordbot.config.items.ShopItem;
import com.blank.humanity.discordbot.database.BuyLogDao;
import com.blank.humanity.discordbot.entities.item.BuyLogEntry;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.item.ItemBuyStatus;

@Service
public class ShopService {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ItemShopConfig itemShopConfig;

    @Autowired
    private BuyLogDao buyLogDao;

    public long amountShopPages() {
	return (long) Math
		.ceil((double) itemShopConfig.getShopItems().size()
			/ (double) itemShopConfig.getItemsPerPage());
    }

    public List<ShopItem> getShopPage(@Valid @Min(1) int page) {
	return itemShopConfig
		.getShopItems()
		.stream()
		.sorted(Comparator.comparing(ShopItem::getOrder))
		.filter(ShopItem::isDisplayed)
		.skip((long) itemShopConfig.getItemsPerPage() * (page - 1))
		.limit(itemShopConfig.getItemsPerPage())
		.toList();
    }

    public Optional<ShopItem> getShopItem(String buyName) {
	return itemShopConfig.getShopItem(buyName);
    }

    @Transactional
    public ItemBuyStatus buyItem(BlankUser user, ShopItem item) {
	return buyItem(user, item, 1);
    }

    public int getAvailableItemAmount(ShopItem item) {
	if(item.getAmountAvailable() == -1) {
	    return -1;
	}
	return item.getAmountAvailable()
		- buyLogDao.sumOfBoughtItems(item.getId());
    }

    @Transactional
    public ItemBuyStatus buyItem(BlankUser user, ShopItem item, int amount) {
	if (user.getBalance() < item.getPrice() * amount) {
	    return ItemBuyStatus.NOT_ENOUGH_MONEY;
	}

	if (getAvailableItemAmount(item) != -1
		&& getAvailableItemAmount(item) < amount) {
	    return ItemBuyStatus.NO_AVAILABLE_SUPPLY;
	}

	blankUserService.decreaseUserBalance(user, item.getPrice() * amount);
	inventoryService.giveItem(user, item.getItemId(), amount);
	buyLogDao
		.save(BuyLogEntry
			.builder()
			.buyer(user)
			.shopId(item.getId())
			.amount(amount)
			.build());
	return ItemBuyStatus.SUCCESS;
    }

}
