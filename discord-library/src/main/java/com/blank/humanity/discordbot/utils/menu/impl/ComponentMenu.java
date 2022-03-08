package com.blank.humanity.discordbot.utils.menu.impl;

import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.blank.humanity.discordbot.exceptions.menu.NonUniqueInteractionId;
import com.blank.humanity.discordbot.services.MenuService;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

@Accessors(chain = true, fluent = true)
public class ComponentMenu implements DiscordMenu {

    private JDA jda;

    private MenuService menuService;

    @Setter
    @Getter
    private Runnable timeoutTask;

    @Getter
    private LinkedHashMap<String, Predicate<ButtonInteractionEvent>> buttonActions = new LinkedHashMap<>();

    @Getter
    private LinkedHashMap<String, Predicate<SelectMenuInteractionEvent>> selectMenuActions = new LinkedHashMap<>();

    private long channelId;

    private long messageId;

    @Getter
    @Setter
    private List<Long> allowedDiscordIds;

    @Getter
    @Setter
    private boolean singleUse = false;

    @Getter
    @Setter(value = AccessLevel.PRIVATE)
    private boolean isDiscarded = false;

    private List<ActionRow> actionRows = new ArrayList<>();

    private boolean forceNewRow = true;

    private TemporalAmount timeout;

    private static AtomicInteger actionCounter = new AtomicInteger(0);

    ComponentMenu(TemporalAmount timeout) {
        this.timeout = timeout;
    }

    private void addOrCreateActionRow(ItemComponent component) {
        if (actionRows.isEmpty()) {
            actionRows.add(ActionRow.of(component));
            forceNewRow = false;
            return;
        }
        ActionRow row = actionRows.get(actionRows.size() - 1);
        boolean validType = row
            .getComponents()
            .stream()
            .map(ItemComponent::getType)
            .allMatch(t -> t.equals(component.getType()));
        if (!validType
            || row.getComponents().size() + 1 > component.getMaxPerRow()) {
            newActionRow();
        }
        if (forceNewRow) {
            actionRows.add(ActionRow.of(component));
            forceNewRow = false;
        } else {
            actionRows
                .get(actionRows.size() - 1)
                .getComponents()
                .add(component);
        }
    }

    public ComponentMenu newActionRow() {
        this.forceNewRow = true;
        return this;
    }

    private String createUniqueId(String id) {
        return actionCounter.getAndIncrement() + "_" + id;
    }

    public ComponentMenu addButton(Button button,
        Predicate<ButtonInteractionEvent> action) {
        button = button.withId(createUniqueId(button.getId()));
        addOrCreateActionRow(button);

        if (button.getStyle() != ButtonStyle.LINK) {
            buttonActions.put(button.getId(), action);
        }
        return this;
    }

    public ComponentMenu addLinkButton(Button button) {
        addOrCreateActionRow(button);
        return this;
    }

    public ComponentMenu addSelectMenu(SelectMenu select,
        Predicate<SelectMenuInteractionEvent> action) {
        select = select
            .createCopy()
            .setId(createUniqueId(select.getId()))
            .build();
        addOrCreateActionRow(select);

        selectMenuActions.put(select.getId(), action);
        return this;
    }

    @Override
    public void buildMenu(JDA jda, Message message, MenuService menuService) {
        this.jda = jda;
        this.menuService = menuService;

        CompletableFuture<Message> request = message
            .editMessageComponents(actionRows)
            .submit();

        buttonActions.entrySet().forEach(entry -> {
            try {
                menuService
                    .registerButtonInteraction(this, entry.getKey(),
                        event -> wrapper(event, entry.getValue()));
            } catch (NonUniqueInteractionId e) {
                throw new RuntimeException(e);
            }
        });

        selectMenuActions.entrySet().forEach(entry -> {
            try {
                menuService
                    .registerSelectMenuInteraction(this, entry.getKey(),
                        event -> wrapper(event, entry.getValue()));
            } catch (NonUniqueInteractionId e) {
                throw new RuntimeException(e);
            }
        });

        menuService.registerDiscordMenuTimeout(this, timeout);

        try {
            message = request.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        this.channelId = message.getChannel().getIdLong();
        this.messageId = message.getIdLong();
    }

    private <E extends GenericComponentInteractionCreateEvent> void wrapper(
        E event, Predicate<E> value) {
        InteractionHook callback = event.deferEdit().complete();
        try {
            if (value.test(event)) {
                if (singleUse()) {
                    discard();
                } else {
                    menuService.registerDiscordMenuTimeout(this, timeout);
                }
            }
        } catch (Exception e) {
            callback.sendMessage(e.getMessage()).complete();
        }
    }

    @Override
    public void discard() {
        if (!isDiscarded()) {
            menuService.discardMenuListeners(this);
            jda
                .getTextChannelById(channelId)
                .editMessageComponentsById(messageId, Collections.emptyList())
                .complete();
            isDiscarded = true;
        }
    }

}
