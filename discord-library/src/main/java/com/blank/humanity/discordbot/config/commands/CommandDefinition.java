package com.blank.humanity.discordbot.config.commands;

import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandDefinition {

    private String description;

    private HashMap<String, String> options;

    private boolean roleRestricted = false;

    private boolean hidden = false;

    private List<Long> allowedRoles = List.of();

    public String getOptionDescription(String string) {
	return options.get(string);
    }

}
