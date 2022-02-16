package com.blank.humanity.discordbot.config.commands.games;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDefinition {

    @NotNull
    private String displayName;

    @NotNull
    private Long cooldownAmount;

    @NotNull
    private ChronoUnit cooldownTimeUnit;

    @NotNull
    private Double winningsMultiplier;

    private Map<String, Object> options;

}
