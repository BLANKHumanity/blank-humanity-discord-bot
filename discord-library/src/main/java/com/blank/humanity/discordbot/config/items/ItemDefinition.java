package com.blank.humanity.discordbot.config.items;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
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

    @Nullable
    private String action;

    private Map<String, Object> actionArguments;

}
