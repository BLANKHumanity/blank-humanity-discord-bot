package com.blank.humanity.discordbot.utils.menu;

import java.util.List;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

@AllArgsConstructor
public class InteractionRestrictionWrapper<E extends GenericComponentInteractionCreateEvent, M extends DiscordMenu>
    implements DiscordMenuActionWrapper<E, M> {

    private List<Long> allowedDiscordIds;

    @Override
    public boolean wrap(DiscordMenuInteraction<E, M> interaction,
        WrapperChain<E, M> chain) {
        if (!allowedDiscordIds
            .contains(interaction.event().getMember().getIdLong())) {
            return false;
        }
        return chain.doNext(interaction.argument());
    }
}
