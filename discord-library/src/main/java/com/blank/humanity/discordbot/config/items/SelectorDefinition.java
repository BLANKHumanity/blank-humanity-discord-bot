package com.blank.humanity.discordbot.config.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectorDefinition {

    private ActionConfigSelector selectorType;

    private String identifier;

}
