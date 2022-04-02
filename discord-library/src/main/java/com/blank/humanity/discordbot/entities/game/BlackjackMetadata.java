package com.blank.humanity.discordbot.entities.game;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlackjackMetadata {

    private List<String> playerCards;
    
    private List<String> dealerCards;
    
    private int betAmount;
    
    private int round;
    
}
