package com.blank.humanity.discordbot.utils.menu;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.Event;

@RequiredArgsConstructor
public class WrapperChain<E extends Event, M extends DiscordMenu> {

    private final DiscordMenuActionWrapper<E, M>[] wrappers;

    private final E event;

    private final M menu;

    private int index = 0;

    public boolean doNext(Object argument) {
        if (wrappers.length > index) {
            DiscordMenuInteraction<E, M> interaction = new DiscordMenuInteraction<>(
                event, menu, argument);
            DiscordMenuActionWrapper<E, M> wrapper = wrappers[index];
            index++;
            return wrapper.wrap(interaction, this);
        }
        return true;
    }

}