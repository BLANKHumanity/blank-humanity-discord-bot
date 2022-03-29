package com.blank.humanity.discordbot.config.items;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDefinition {

    @Min(0)
    @NotNull(message = "id null")
    private int id;

    @NotNull(message = "name null")
    private String name;

    @NotNull(message = "Description null")
    private String description;

    @NotNull
    private String useName;

    @NotNull
    private ItemActionDefinition[] actions;
    
}
