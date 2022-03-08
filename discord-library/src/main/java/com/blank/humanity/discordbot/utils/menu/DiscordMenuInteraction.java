package com.blank.humanity.discordbot.utils.menu;

import net.dv8tion.jda.api.events.Event;

public record DiscordMenuInteraction<E extends Event, M extends DiscordMenu> (
    E event, M menu, Object argument) {
}