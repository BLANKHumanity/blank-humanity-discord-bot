package com.blank.humanity.discordbot.wallet.command.emotes;

import com.blank.humanity.discordbot.BlankEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class InitializerLearnsEmoteEvent extends BlankEvent {

    private int initializer;

    private String emote;

}
