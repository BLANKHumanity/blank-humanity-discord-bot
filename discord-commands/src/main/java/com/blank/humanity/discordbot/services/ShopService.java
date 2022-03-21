package com.blank.humanity.discordbot.services;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blank.humanity.discordbot.config.items.ItemShopConfig;
import com.blank.humanity.discordbot.config.items.ShopItem;
import com.blank.humanity.discordbot.database.BuyLogDao;
import com.blank.humanity.discordbot.entities.item.BuyLogEntry;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.exceptions.economy.NotEnoughBalanceException;
import com.blank.humanity.discordbot.utils.item.ItemBuyStatus;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final BlankUserService blankUserService;
    private final InventoryService inventoryService;
    private final ItemShopConfig itemShopConfig;
    private final BuyLogDao buyLogDao;

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
        if (item.getAmountAvailable() == -1) {
            return -1;
        }
        return item.getAmountAvailable()
            - buyLogDao.sumOfBoughtItems(item.getId());
    }

    @Transactional
    @Nonnull
    public ItemBuyStatus buyItem(BlankUser user, ShopItem item, int amount) {
        if (user.getBalance() < item.getPrice() * amount) {
            return ItemBuyStatus.NOT_ENOUGH_MONEY;
        }

        if (getAvailableItemAmount(item) != -1
            && getAvailableItemAmount(item) < amount) {
            return ItemBuyStatus.NO_AVAILABLE_SUPPLY;
        }

        try {
            blankUserService
                .decreaseUserBalance(user, item.getPrice() * amount);
        } catch (NotEnoughBalanceException e) {
            return ItemBuyStatus.NOT_ENOUGH_MONEY;
        }
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

    public Collection<Choice> autoCompleteShopItems(String partialItemName) {
        return itemShopConfig
            .getShopItems()
            .stream()
            .map(item -> Pair
                .of(item,
                    inventoryService
                        .getItemDefinition(item.getItemId())
                        .orElseThrow()))
            .filter(pair -> pair
                .getFirst()
                .getBuyName()
                .toLowerCase()
                .contains(partialItemName.toLowerCase())
                || pair
                    .getSecond()
                    .getName()
                    .toLowerCase()
                    .contains(partialItemName.toLowerCase()))
            .map(pair -> new Choice(pair.getSecond().getName(),
                pair.getFirst().getBuyName()))
            .toList();
    }

}
