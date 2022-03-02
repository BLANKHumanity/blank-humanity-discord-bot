package com.blank.humanity.discordbot.config.items;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix="shop")
public class ItemShopConfig {

    @NotNull
    @Min(1)
    private int itemsPerPage = 8;
    
    @NotNull
    private List<ShopItem> shopItems;

    public Optional<ShopItem> getShopItem(int id) {
	return shopItems
		.stream()
		.filter(item -> item.getId() == id)
		.findFirst();
    }

    public Optional<ShopItem> getShopItem(String buyName) {
	return shopItems
		.stream()
		.filter(item -> item.getBuyName().equalsIgnoreCase(buyName))
		.findFirst();
    }

}
