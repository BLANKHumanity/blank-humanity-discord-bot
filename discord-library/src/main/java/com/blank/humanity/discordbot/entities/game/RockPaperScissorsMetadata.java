package com.blank.humanity.discordbot.entities.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RockPaperScissorsMetadata {

    private int betAmount;

}
