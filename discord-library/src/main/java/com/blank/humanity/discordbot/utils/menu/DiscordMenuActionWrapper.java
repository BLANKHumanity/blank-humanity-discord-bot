package com.blank.humanity.discordbot.utils.menu;

import net.dv8tion.jda.api.events.Event;

public interface DiscordMenuActionWrapper<E extends Event, M extends DiscordMenu> {

    public boolean wrap(DiscordMenuInteraction<E, M> interaction,
        WrapperChain<E, M> chain);

}
