package com.blank.humanity.discordbot.services;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.exceptions.menu.NonUniqueInteractionIdException;
import com.blank.humanity.discordbot.utils.Wrapper;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Service
public class MenuServiceImpl implements MenuService, EventListener {

    @Autowired
    private JDA jda;

    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private TransactionExecutor executor;

    private static Map<DiscordMenu, List<Object>> menuMap = new HashMap<>();

    private static Map<DiscordMenu, ScheduledFuture<?>> menuFutures = new HashMap<>();

    private static Map<String, Consumer<ButtonInteractionEvent>> buttonListeners = new HashMap<>();

    private static Map<String, Consumer<SelectMenuInteractionEvent>> selectMenuListeners = new HashMap<>();

    private static Map<Long, Consumer<MessageReactionAddEvent>> reactionAddListeners = new HashMap<>();

    @PostConstruct
    void setupMenuService() {
        jda.addEventListener(this);
    }

    @Override
    public void registerButtonInteraction(@NonNull DiscordMenu menu,
        String uniqueId,
        @NonNull Consumer<ButtonInteractionEvent> eventConsumer)
        throws NonUniqueInteractionIdException {
        if (buttonListeners.containsKey(uniqueId)) {
            throw new NonUniqueInteractionIdException(uniqueId);
        }
        addIdToMenuMap(menu, uniqueId);
        buttonListeners.put(uniqueId, eventConsumer);
    }

    @Override
    public void registerSelectMenuInteraction(@NonNull DiscordMenu menu,
        String uniqueId,
        @NonNull Consumer<SelectMenuInteractionEvent> eventConsumer)
        throws NonUniqueInteractionIdException {
        if (selectMenuListeners.containsKey(uniqueId)) {
            throw new NonUniqueInteractionIdException(uniqueId);
        }
        addIdToMenuMap(menu, uniqueId);
        selectMenuListeners.put(uniqueId, eventConsumer);
    }

    @Override
    public void registerReactionAddInteraction(@NonNull DiscordMenu menu,
        long messageId,
        @NonNull Consumer<MessageReactionAddEvent> eventConsumer)
        throws NonUniqueInteractionIdException {
        if (reactionAddListeners.containsKey(messageId)) {
            throw new NonUniqueInteractionIdException(String.valueOf(messageId));
        }
        addIdToMenuMap(menu, messageId);
        reactionAddListeners.put(messageId, eventConsumer);
    }

    private void addIdToMenuMap(DiscordMenu menu, Object id) {
        menuMap.computeIfAbsent(menu, unused -> new ArrayList<>()).add(id);
    }

    @Override
    public void registerDiscordMenuTimeout(DiscordMenu menu,
        TemporalAmount timeout) {
        menuFutures
            .merge(menu,
                scheduler
                    .schedule(menu::timeout,
                        OffsetDateTime.now().plus(timeout).toInstant()),
                (oldTask, newTask) -> {
                    oldTask.cancel(true);
                    return newTask;
                });
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ButtonInteractionEvent buttonEvent) {
            Optional
                .ofNullable(buttonListeners.get(buttonEvent.getComponentId()))
                .map(consumer -> Wrapper.wrap(consumer, buttonEvent))
                .ifPresent(this::executeInTransaction);
        } else if (event instanceof SelectMenuInteractionEvent selectMenuEvent) {
            Optional
                .ofNullable(
                    selectMenuListeners.get(selectMenuEvent.getComponentId()))
                .map(consumer -> Wrapper.wrap(consumer, selectMenuEvent))
                .ifPresent(this::executeInTransaction);
        } else if (event instanceof MessageReactionAddEvent reactionAddEvent
            && !reactionAddEvent.getUser().isBot()) {
            Optional
                .ofNullable(reactionAddListeners
                    .get(reactionAddEvent.getMessageIdLong()))
                .map(consumer -> Wrapper.wrap(consumer, reactionAddEvent))
                .ifPresent(this::executeInTransaction);
        }
    }

    private void executeInTransaction(Runnable run) {
        executor.executeAsTransactionSync(Wrapper.transactionCallback(run));
    }

    @Override
    public void discardMenuListeners(DiscordMenu menu) {
        if (menuMap.containsKey(menu)) {
            menuMap.get(menu).forEach(key -> {
                buttonListeners.remove(key);
                selectMenuListeners.remove(key);
                reactionAddListeners.remove(key);
            });
        }
        if (menuFutures.containsKey(menu)) {
            menuFutures.get(menu).cancel(false);
        }
    }

}
