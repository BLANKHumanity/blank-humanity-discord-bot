package com.blank.humanity.discordbot.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import com.blank.humanity.discordbot.config.items.ItemConfiguration;
import com.blank.humanity.discordbot.config.items.ItemShopConfig;
import com.blank.humanity.discordbot.config.items.ShopItem;
import com.blank.humanity.discordbot.database.BuyLogDao;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.exceptions.economy.NotEnoughBalanceException;
import com.blank.humanity.discordbot.service.ServiceIntegrationTest;
import com.blank.humanity.discordbot.utils.item.ItemBuyStatus;

import net.dv8tion.jda.api.interactions.commands.Command.Choice;

@Disabled
@Rollback(true)
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, value = "/shopServiceTestData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, value = "/clearShopServiceTestData.sql")
class ShopServiceTest extends ServiceIntegrationTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemShopConfig itemShopConfig;

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ItemConfiguration itemConfiguration;

    @Autowired
    private BuyLogDao buyLogDao;

    private final double SHOP_ITEMS = 5;

    private final double ITEMS_PER_PAGE = 2;

    private final int SOME_BOUGHT_SHOP_ID = 1;

    private final int ALL_BOUGHT_SHOP_ID = 2;

    private final int NONE_BOUGHT_SHOP_ID = 3;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(blankUserService);
        Mockito.reset(inventoryService);
    }

    @Test
    void testAvailableItemAmountZeroBought() {
        int availableAtStart = 50;

        ShopItem item = itemShopConfig.getShopItem(4).get();

        int available = shopService.getAvailableItemAmount(item);
        assertThat(available).isEqualTo(availableAtStart);
    }

    @Test
    void testAvailableItemAmountSomeBought() {
        int availableAtStart = 50;

        ShopItem item = itemShopConfig.getShopItem(SOME_BOUGHT_SHOP_ID).get();

        int available = shopService.getAvailableItemAmount(item);
        assertThat(available).isEqualTo(availableAtStart - 3);
    }

    @Test
    void testAvailableItemAmountAllBought() {
        ShopItem item = itemShopConfig.getShopItem(ALL_BOUGHT_SHOP_ID).get();

        int available = shopService.getAvailableItemAmount(item);
        assertThat(available).isZero();
    }

    @Test
    void testAvailableItemAmountUnlimited() {
        ShopItem item = itemShopConfig.getShopItem(NONE_BOUGHT_SHOP_ID).get();

        int available = shopService.getAvailableItemAmount(item);
        assertThat(available).isEqualTo(-1);
    }

    @Test
    void testGetShopPage() {
        List<ShopItem> items = shopService.getShopPage(2);

        assertThat(items).isNotNull().doesNotContainNull().hasSize(2);
        assertThat(items.get(0).getItemId()).isEqualTo(3);
        assertThat(items.get(1).getItemId()).isEqualTo(2);
    }

    @Test
    void testAmountShopPages() {
        long shouldBe = (long) Math.ceil(SHOP_ITEMS / ITEMS_PER_PAGE);
        assertThat(shopService.amountShopPages()).isEqualTo(shouldBe);
    }

    @Test
    void testBuyItemSingle() throws NotEnoughBalanceException {
        BlankUser user = new BlankUser();
        user.setBalance(1234);
        user.setId(1l);

        ShopItem item = itemShopConfig.getShopItem(NONE_BOUGHT_SHOP_ID).get();

        ItemBuyStatus status = shopService.buyItem(user, item);
        assertThat(status).isEqualTo(ItemBuyStatus.SUCCESS);

        verify(blankUserService).decreaseUserBalance(user, item.getPrice());
        verify(inventoryService).giveItem(user, item.getItemId(), 1);

        assertThat(buyLogDao.sumOfBoughtItems(item.getId())).isEqualTo(1);
    }

    @Test
    void testBuyItemMultiple() throws NotEnoughBalanceException {
        int amount = 6;

        BlankUser user = new BlankUser();
        user.setBalance(1234);
        user.setId(1l);

        ShopItem item = itemShopConfig.getShopItem(NONE_BOUGHT_SHOP_ID).get();

        ItemBuyStatus status = shopService.buyItem(user, item, amount);
        assertThat(status).isEqualTo(ItemBuyStatus.SUCCESS);

        verify(blankUserService)
            .decreaseUserBalance(user, amount * item.getPrice());
        verify(inventoryService).giveItem(user, item.getItemId(), amount);

        assertThat(buyLogDao.sumOfBoughtItems(item.getId())).isEqualTo(amount);
    }

    @Test
    void testBuyItemNotEnoughBalance() throws NotEnoughBalanceException {
        BlankUser user = new BlankUser();
        user.setBalance(50);
        user.setId(1l);

        ShopItem item = itemShopConfig.getShopItem(NONE_BOUGHT_SHOP_ID).get();

        ItemBuyStatus status = shopService.buyItem(user, item);
        assertThat(status).isEqualTo(ItemBuyStatus.NOT_ENOUGH_MONEY);

        verify(blankUserService, never())
            .decreaseUserBalance(user, item.getPrice());
        verify(inventoryService, never()).giveItem(user, item.getItemId(), 1);

        assertThat(buyLogDao.sumOfBoughtItems(item.getId())).isZero();
    }

    @Test
    void testBuyItemNoSupply() throws NotEnoughBalanceException {
        BlankUser user = new BlankUser();
        user.setBalance(1234);
        user.setId(1l);

        ShopItem item = itemShopConfig.getShopItem(ALL_BOUGHT_SHOP_ID).get();

        ItemBuyStatus status = shopService.buyItem(user, item);
        assertThat(status).isEqualTo(ItemBuyStatus.NO_AVAILABLE_SUPPLY);

        verify(blankUserService, never())
            .decreaseUserBalance(user, item.getPrice());
        verify(inventoryService, never()).giveItem(user, item.getItemId(), 1);

        assertThat(buyLogDao.sumOfBoughtItems(item.getId())).isEqualTo(20);
    }

    @Test
    void testAutoCompleteAll() {
        when(inventoryService.getItemDefinition(Mockito.anyInt()))
            .then(invocation -> itemConfiguration
                .getItemDefinition(invocation.getArgument(0, Integer.class)));

        Collection<Choice> choices = shopService.autoCompleteShopItems("");

        assertThat(choices)
            .isNotNull()
            .doesNotContainNull()
            .hasSize(5)
            .anyMatch(choice -> choice.getName().equals("Kingpin"));
    }

    @Test
    void testAutoCompleteFiltered() {
        when(inventoryService.getItemDefinition(Mockito.anyInt()))
            .then(invocation -> itemConfiguration
                .getItemDefinition(invocation.getArgument(0, Integer.class)));

        Collection<Choice> choices = shopService.autoCompleteShopItems("First");

        assertThat(choices)
            .isNotNull()
            .doesNotContainNull()
            .hasSize(1)
            .allMatch(choice -> choice.getName().equals("First Class Ticket"));
    }
}
