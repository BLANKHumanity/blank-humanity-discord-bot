package com.blank.humanity.discordbot.entities.game;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouletteMetadata {

    private int betAmount;

    private int round;

    private List<Integer> previousBetAmounts;
    
}
