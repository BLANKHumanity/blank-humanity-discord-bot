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
import de.zorro909.blank.BlankDiscordBot.database.BuyLogDao;
import de.zorro909.blank.BlankDiscordBot.database.ItemDao;
import de.zorro909.blank.BlankDiscordBot.entities.item.BuyLogEntry;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
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
    private BuyLogDao buyLogDao;

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
	return buyItem(user, item, 1);
    }

    public int getAvailableItemAmount(ShopItem item) {
	return item.getAmountAvailable()
		- buyLogDao.sumOfBoughtItems(item.getId());
    }

    @Transactional
    public ItemBuyStatus buyItem(BlankUser user, ShopItem item, int amount) {
	if (user.getBalance() < item.getPrice() * amount) {
	    return ItemBuyStatus.NOT_ENOUGH_MONEY;
	}

	if (getAvailableItemAmount(item) != -1 && getAvailableItemAmount(item) < amount) {
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
