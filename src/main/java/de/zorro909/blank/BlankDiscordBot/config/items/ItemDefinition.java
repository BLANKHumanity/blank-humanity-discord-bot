package de.zorro909.blank.BlankDiscordBot.config.items;

import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import de.zorro909.blank.BlankDiscordBot.services.item.ItemAction;
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
    private ItemAction action;

    private Map<String, Object> actionArguments;

}
