package com.blank.humanity.discordbot.funplace;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.InventoryService;

import net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Service
public class FunPlaceShopService {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private FunPlaceItemShopConfig funPlaceItemShopConfig;

    @Autowired
    private FunPlaceBuyLogDao buyLogDao;

    public long amountShopPages() {
        return (long) Math
            .ceil((double) funPlaceItemShopConfig.getShopItems().size()
                / (double) funPlaceItemShopConfig.getItemsPerPage());
    }

    public List<ShopItem> getShopPage(@Valid @Min(1) int page) {
        return funPlaceItemShopConfig
            .getShopItems()
            .stream()
            .sorted(Comparator.comparing(ShopItem::getOrder))
            .filter(ShopItem::isDisplayed)
            .skip((long) funPlaceItemShopConfig.getItemsPerPage()
                * (page - 1))
            .limit(funPlaceItemShopConfig.getItemsPerPage())
            .toList();
    }

    public Optional<ShopItem> getShopItem(String buyName) {
        return funPlaceItemShopConfig.getShopItem(buyName);
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
            .save(FunPlaceBuyLogEntry
                .builder()
                .buyer(user)
                .shopId(item.getId())
                .amount(amount)
                .build());
        return ItemBuyStatus.SUCCESS;
    }

    public Collection<Choice> autoCompleteShopItems(String partialItemName) {
        return funPlaceItemShopConfig
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
