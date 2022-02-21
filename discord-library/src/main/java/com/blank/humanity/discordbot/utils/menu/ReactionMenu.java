package com.blank.humanity.discordbot.utils.menu;

import static com.blank.humanity.discordbot.utils.Wrapper.wrap;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAmount;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.springframework.scheduling.TaskScheduler;

import com.blank.humanity.discordbot.services.TransactionExecutor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;

@Slf4j
@Accessors(chain = true, fluent = true)
public class ReactionMenu extends ListenerAdapter {

    private JDA jda;

    private long guildChannelId;

    private long messageId;

    private TemporalAmount timeout;

    @Setter
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

    private ScheduledFuture<?> futureRemoval;

    private TransactionExecutor transactionExecutor;

    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private boolean isDiscarded = false;

    public ReactionMenu(TemporalAmount timeout) {
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

    public void buildMenu(JDA jda, Message message, TaskScheduler scheduler,
        TransactionExecutor transactionExecutor) {
        this.jda = jda;
        this.messageId = message.getIdLong();
        this.guildChannelId = message.getChannel().getIdLong();
        this.transactionExecutor = transactionExecutor;

        this.jda.addEventListener(this);

        menuActions
            .keySet()
            .stream()
            .parallel()
            .map(message::addReaction)
            .forEach(RestAction::complete);

        futureRemoval = scheduler
            .schedule(this::timeout,
                OffsetDateTime.now().plus(timeout).toInstant());
    }

    public void timeout() {
        discard();
        timeoutTask.run();
    }

    public void discard() {
        if (!isDiscarded()) {
            futureRemoval.cancel(false);
            this.jda.removeEventListener(this);
            this.jda
                .getTextChannelById(guildChannelId)
                .clearReactionsById(messageId)
                .complete();
            isDiscarded(true);
        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (messageId == event.getMessageIdLong() && !event.getUser().isBot()) {
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

            Predicate<MessageReactionAddEvent> action = menuActions
                .get(reactionCode);
            transactionExecutor
                .executeAsTransaction(status -> action.test(event),
                    wrap(this::reactionMenuInteractionErrorHandler, event),
                    wrap(this::finishReactionMenuInteraction, event));
        }
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
        }
    }

}
