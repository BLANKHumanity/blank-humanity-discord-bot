package de.zorro909.blank.BlankDiscordBot.utils.menu;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAmount;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.springframework.scheduling.TaskScheduler;
import de.zorro909.blank.BlankDiscordBot.services.TransactionExecutor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Accessors(chain = true, fluent = true)
public class ReactionMenu extends ListenerAdapter {

    private JDA jda;

    private long messageId;

    private TemporalAmount timeout;

    @Setter
    private Runnable timeoutTask;

    @Getter
    private LinkedHashMap<String, Function<MessageReactionAddEvent, Boolean>> menuActions = new LinkedHashMap<>();

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

    public ReactionMenu(TemporalAmount timeout) {
	this.timeout = timeout;
    }

    /**
     * @param emoji Needs to be either a Unicode Emoji or a custom emoji with
     *              the pattern <:name:id>
     */
    public void addMenuAction(String emoji,
	    Function<MessageReactionAddEvent, Boolean> action) {
	this.menuActions.put(emoji, action);
    }

    public void buildMenu(JDA jda, Message message, TaskScheduler scheduler,
	    TransactionExecutor transactionExecutor) {
	this.jda = jda;
	this.messageId = message.getIdLong();
	this.transactionExecutor = transactionExecutor;

	for (String emoji : menuActions.keySet()) {
	    message.addReaction(emoji).complete();
	}

	this.jda.addEventListener(this);

	futureRemoval = scheduler
		.schedule(this::timeout,
			OffsetDateTime.now().plus(timeout).toInstant());
    }

    public void timeout() {
	discard();
	timeoutTask.run();
    }

    public void discard() {
	futureRemoval.cancel(false);
	this.jda.removeEventListener(this);
    }

    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
	if (messageId == event.getMessageIdLong() && !event.getUser().isBot()) {
	    String reactionCode = event.getReactionEmote().getAsReactionCode();
	    if (!menuActions.containsKey(reactionCode)) {
		event.getReaction().removeReaction().queue();
		return;
	    }

	    Member member = event.retrieveMember().complete();
	    if (restricted) {
		if (!allowedDiscordIds.contains(member.getUser().getIdLong())) {
		    event.getReaction().removeReaction().queue();
		    return;
		}
	    }

	    Function<MessageReactionAddEvent, Boolean> action = menuActions
		    .get(reactionCode);
	    transactionExecutor
		    .executeAsTransaction((status) -> action.apply(event),
			    (e) -> {
				e.printStackTrace();
				event.getReaction().removeReaction().queue();
			    }, (success) -> {
				if (success != null && success == false) {
				    event
					    .getReaction()
					    .removeReaction()
					    .queue();
				    return;
				}
				if (singleUse) {
				    discard();
				}
			    });
	}
    }

}