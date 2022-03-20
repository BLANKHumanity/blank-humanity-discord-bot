package com.blank.humanity.discordbot.config.items;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopItem {

    @NotNull
    @Min(0)
    private int id;

    @NotNull
    @Min(0)
    private int itemId;

    @NotNull
    @NotBlank
    private String buyName;

    @NotNull
    @Min(1)
    private int price;

    /**
     * Limits buyable Amount of a Shop Item, -1 sets the Availability to
     * Infinity
     */
    @NotNull
    @Min(-1)
    private int amountAvailable;

    @NotNull
    private boolean displayed = true;

    @NotNull
    private int order = Integer.MAX_VALUE;

}
