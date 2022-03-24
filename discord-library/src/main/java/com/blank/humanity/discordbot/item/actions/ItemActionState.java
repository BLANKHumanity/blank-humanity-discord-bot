package com.blank.humanity.discordbot.item.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;

import com.blank.humanity.discordbot.config.items.ActionConfigSelector;
import com.blank.humanity.discordbot.config.items.ItemActionDefinition;
import com.blank.humanity.discordbot.config.items.ItemDefinition;
import com.blank.humanity.discordbot.config.items.SelectorDefinition;
import com.blank.humanity.discordbot.config.messages.CustomFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.services.MessageService;
import com.blank.humanity.discordbot.utils.FormatDataKey;
import com.blank.humanity.discordbot.utils.FormattingData;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@RequiredArgsConstructor
public class ItemActionState {

    private final long replyChannelId;

    @Getter
    private final ItemDefinition itemDefinition;

    @Getter
    private final int amount;

    @Getter
    private List<MessageEmbed> embedsToReply = new LinkedList<>();

    @Getter
    private Map<Long, List<MessageEmbed>> embedsToSend = new HashMap<>();

    private Map<String, Object> environment = new HashMap<>();

    @Getter
    @Setter
    private int actionIndex = 0;

    private final MessageService messageService;

    public void reply(MessageEmbed... embeds) {
        if (embeds.length == 0)
            return;

        setSelector(ActionConfigSelector.CHANNEL, replyChannelId);
        setSelector(ActionConfigSelector.MESSAGE,
            embeds[embeds.length - 1].getDescription());
        Collections.addAll(embedsToReply, embeds);
    }

    public void reply(FormattingData... formattingDatas) {
        Map<FormatDataKey, Object> environmentDataKeys = generateEnvironmentFormattingKeys();
        Arrays
            .stream(formattingDatas)
            .forEach(data -> reply(data, environmentDataKeys));
    }

    public void reply(FormattingData formattingData) {
        reply(formattingData, generateEnvironmentFormattingKeys());
    }

    private void reply(FormattingData formattingData,
        Map<FormatDataKey, Object> environmentDataKeys) {
        if (formattingData.containsKey(GenericFormatDataKey.USER_MENTION)) {
            setSelector(ActionConfigSelector.TARGET_USER,
                formattingData.get(GenericFormatDataKey.USER_MENTION));
        }
        reply(generateEmbed(formattingData, environmentDataKeys));
    }

    public void sendMessage(long channelId, MessageEmbed... embeds) {
        if (embeds.length == 0)
            return;

        setSelector(ActionConfigSelector.CHANNEL, channelId);
        setSelector(ActionConfigSelector.MESSAGE,
            embeds[embeds.length - 1].getDescription());

        List<MessageEmbed> channelEmbedsToSend = embedsToSend
            .computeIfAbsent(channelId, id -> new LinkedList<>());
        Collections.addAll(channelEmbedsToSend, embeds);
    }

    public void sendMessage(long channelId, FormattingData... formattingDatas) {
        Map<FormatDataKey, Object> environmentDataKeys = generateEnvironmentFormattingKeys();
        Arrays
            .stream(formattingDatas)
            .forEach(data -> sendMessage(channelId, data, environmentDataKeys));
    }

    public void sendMessage(long channelId, FormattingData formattingData) {
        sendMessage(channelId, formattingData,
            generateEnvironmentFormattingKeys());
    }

    private void sendMessage(long channelId, FormattingData formattingData,
        Map<FormatDataKey, Object> environmentDataKeys) {
        if (formattingData.containsKey(GenericFormatDataKey.USER_MENTION)) {
            setSelector(ActionConfigSelector.TARGET_USER,
                formattingData.get(GenericFormatDataKey.USER_MENTION));
        }
        sendMessage(channelId,
            generateEmbed(formattingData, environmentDataKeys));
    }

    private MessageEmbed generateEmbed(FormattingData formattingData,
        Map<FormatDataKey, Object> environmentDataKeys) {
        environmentDataKeys.putAll(formattingData.dataPairings());
        formattingData.dataPairings(environmentDataKeys);
        String message = messageService.format(formattingData);
        return new EmbedBuilder().setDescription(message).build();
    }

    private Map<FormatDataKey, Object> generateEnvironmentFormattingKeys() {
        return environment
            .entrySet()
            .parallelStream()
            .map(entry -> generateFormatDataKey(entry.getKey(),
                entry.getValue().toString()))
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private Pair<FormatDataKey, String> generateFormatDataKey(String key,
        String value) {
        return Pair.of(CustomFormatDataKey.key(key), value);
    }

    public void setSelector(ActionConfigSelector selector, Object value) {
        environment.put(selector.toString() + "_" + actionIndex, value);
        environment.put(selector.toString() + "_LATEST", value);
    }

    public void setEnvironment(String key, Object value) {
        environment.put(key, value);
    }

    public Object getProperty(String key, Object defaultValue) {
        ItemActionDefinition actionDefinition = itemDefinition
            .getActions()[actionIndex];
        var selectors = actionDefinition.getSelectors();
        if (selectors.containsKey(key)) {
            return resolveSelector(selectors.get(key));
        }
        var actionArguments = actionDefinition.getActionArguments();
        if (actionArguments.containsKey(key)) {
            return actionArguments.get(key);
        }
        return environment.getOrDefault(key, defaultValue);
    }

    public Object getProperty(String key) {
        return getProperty(key, (Object) null);
    }

    public <T> T getProperty(String key, @NonNull Function<String, T> mapper,
        T defaultValue) {
        return Optional
            .ofNullable(getProperty(key))
            .map(Object::toString)
            .map(mapper::apply)
            .orElse(defaultValue);
    }

    public String getStringProperty(String key) {
        return getProperty(key, Object::toString);
    }

    public String getStringProperty(String key, String defaultValue) {
        return getProperty(key, Object::toString, defaultValue);
    }

    public <T> T getProperty(String key, @NonNull Function<String, T> mapper) {
        return getProperty(key, mapper, null);
    }

    private Object resolveSelector(SelectorDefinition selectorDefinition) {
        ActionConfigSelector selector = selectorDefinition.getSelectorType();
        if (selector == ActionConfigSelector.ENVIRONMENT) {
            return getProperty(selectorDefinition.getIdentifier());
        } else {
            return getProperty(selector.toString() + "_"
                + selectorDefinition.getIdentifier().toUpperCase());
        }
    }

}
