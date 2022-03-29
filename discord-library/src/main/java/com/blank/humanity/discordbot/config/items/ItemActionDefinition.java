package com.blank.humanity.discordbot.config.items;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemActionDefinition {

    @NotNull
    private String action;

    @NotNull
    private Map<String, SelectorDefinition> selectors = new HashMap<>();
    
    @NotNull
    private Map<String, Object> actionArguments = new HashMap<>();

}
