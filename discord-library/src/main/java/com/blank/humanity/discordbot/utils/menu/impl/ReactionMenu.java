package com.blank.humanity.discordbot.utils.menu.impl;

import java.time.temporal.TemporalAmount;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.blank.humanity.discordbot.exceptions.menu.NonUniqueInteractionId;
import com.blank.humanity.discordbot.services.MenuService;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;

@Slf4j
@Accessors(chain = true, fluent = true)
public class ReactionMenu implements DiscordMenu {

    private JDA jda;

    private long guildChannelId;

    private long messageId;

    private TemporalAmount timeout;

    private MenuService menuService;

    @Setter
    @Getter
    private Runnable timeoutTask;

    @Getter
    private LinkedHashMap<String, Predicate<MessageReactionAddEvent>> menuActions = new LinkedHashMap<>();

    @Getter
    @Setter
    private boolean restricted = false;

    @Getter
    @Setter
    private List<Long> allowedDiscordIds;

    @Getter
    @Setter
    private boolean singleUse = false;

    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private boolean isDiscarded = false;

    ReactionMenu(TemporalAmount timeout) {
        this.timeout = timeout;
    }

    /**
     * @param emoji Needs to be either a Unicode Emoji or a custom emoji with
     *              the pattern <:name:id>
     */
    public void addMenuAction(String emoji,
        Predicate<MessageReactionAddEvent> action) {
        this.menuActions.put(emoji, action);
    }

    public void buildMenu(JDA jda, Message message, MenuService menuService) {
        this.jda = jda;
        this.messageId = message.getIdLong();
        this.guildChannelId = message.getChannel().getIdLong();
        this.menuService = menuService;

        menuActions
            .keySet()
            .stream()
            .map(message::addReaction)
            .forEach(RestAction::complete);

        try {
            this.menuService
                .registerReactionAddInteraction(this, messageId,
                    this::onMessageReactionAdd);
        } catch (NonUniqueInteractionId e) {
            log
                .error(
                    "An Exception occured during reaction interaction registration",
                    e);
            throw new RuntimeException(e);
        }

        this.menuService.registerDiscordMenuTimeout(this, timeout);
    }

    public void discard() {
        if (!isDiscarded()) {
            this.menuService.discardMenuListeners(this);
            this.jda
                .getTextChannelById(guildChannelId)
                .clearReactionsById(messageId)
                .complete();
            isDiscarded = true;
        }
    }

    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        String reactionCode = event.getReactionEmote().getAsReactionCode();
        if (!menuActions.containsKey(reactionCode)) {
            event.getReaction().removeReaction().queue();
            return;
        }

        Member member = event.retrieveMember().complete();
        if (restricted
            && !allowedDiscordIds.contains(member.getUser().getIdLong())) {
            event.getReaction().removeReaction().queue();
            return;
        }

        Boolean success = null;
        try {
            success = menuActions.get(reactionCode).test(event);
        } catch (Exception exception) {
            reactionMenuInteractionErrorHandler(event, exception);
        }
        finishReactionMenuInteraction(event, success);
    }

    private void reactionMenuInteractionErrorHandler(
        MessageReactionAddEvent event, Throwable exception) {
        log
            .error(
                "An exception was thrown during a ReactionMenu Interaction",
                exception);
        event.getReaction().removeReaction().queue();
    }

    private void finishReactionMenuInteraction(MessageReactionAddEvent event,
        Boolean success) {
        if (success != null && !success) {
            event
                .getReaction()
                .removeReaction()
                .queue();
            return;
        }
        if (singleUse) {
            discard();
        } else {
            menuService.registerDiscordMenuTimeout(this, timeout);
        }
    }

}
