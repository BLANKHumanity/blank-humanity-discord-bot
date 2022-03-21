package com.blank.humanity.discordbot.utils.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

@ExtendWith(MockitoExtension.class)
class InteractionRestrictionWrapperTest {

    @Test
    void testInteractionRestrictionSuccess(
        @Mock GenericComponentInteractionCreateEvent interactionEvent,
        @Mock Member member,
        @Mock WrapperChain<GenericComponentInteractionCreateEvent, DiscordMenu> chain) {

        long memberId = 5414239l;
        String testArgument = "TestArgument";

        InteractionRestrictionWrapper<GenericComponentInteractionCreateEvent, DiscordMenu> restrictionWrapper = new InteractionRestrictionWrapper<>(
            List.of(123451l, memberId, 5693l));

        when(member.getIdLong()).thenReturn(memberId);
        when(interactionEvent.getMember()).thenReturn(member);

        DiscordMenuInteraction<GenericComponentInteractionCreateEvent, DiscordMenu> interaction = new DiscordMenuInteraction<GenericComponentInteractionCreateEvent, DiscordMenu>(
            interactionEvent, null, testArgument);

        assertThat(restrictionWrapper.wrap(interaction, chain)).isFalse();

        verify(chain).doNext(testArgument);
    }

    @Test
    void testInteractionRestrictionFail(
        @Mock GenericComponentInteractionCreateEvent interactionEvent,
        @Mock Member member,
        @Mock WrapperChain<GenericComponentInteractionCreateEvent, DiscordMenu> chain) {

        long memberId = 5414239l;

        InteractionRestrictionWrapper<GenericComponentInteractionCreateEvent, DiscordMenu> restrictionWrapper = new InteractionRestrictionWrapper<>(
            Collections.emptyList());

        when(member.getIdLong()).thenReturn(memberId);
        when(interactionEvent.getMember()).thenReturn(member);

        DiscordMenuInteraction<GenericComponentInteractionCreateEvent, DiscordMenu> interaction = new DiscordMenuInteraction<GenericComponentInteractionCreateEvent, DiscordMenu>(
            interactionEvent, null, null);

        assertThat(restrictionWrapper.wrap(interaction, chain)).isFalse();

        verify(chain, never()).doNext(Mockito.any());
    }

}
