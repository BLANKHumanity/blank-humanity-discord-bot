package com.blank.humanity.discordbot.services;

import java.time.temporal.TemporalAmount;
import java.util.function.Consumer;

import com.blank.humanity.discordbot.exceptions.menu.NonUniqueInteractionId;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface MenuService {

    public void registerButtonInteraction(DiscordMenu menu, String uniqueId,
        Consumer<ButtonInteractionEvent> eventConsumer)
        throws NonUniqueInteractionId;

    public void registerSelectMenuInteraction(DiscordMenu menu, String uniqueId,
        Consumer<SelectMenuInteractionEvent> eventConsumer)
        throws NonUniqueInteractionId;

    public void registerReactionAddInteraction(DiscordMenu menu, long messageId,
        Consumer<MessageReactionAddEvent> eventConsumer)
        throws NonUniqueInteractionId;

    public void registerDiscordMenuTimeout(DiscordMenu menu,
        TemporalAmount timeout);
    
    public void discardMenuListeners(DiscordMenu menu);

}
