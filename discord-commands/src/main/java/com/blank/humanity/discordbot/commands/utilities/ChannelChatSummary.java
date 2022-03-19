package com.blank.humanity.discordbot.commands.utilities;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.utilities.messages.UtilityFormatDataKey;
import com.blank.humanity.discordbot.commands.utilities.messages.UtilityMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.TimeUtil;

@Component
public class ChannelChatSummary extends AbstractCommand {

    private static final String CHANNEL = "channel";
    private static final String HOURS = "hours";
    private static final String START_MESSAGE_ID = "startmessageid";

    @Override
    public String getCommandName() {
        return "chatsummary";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData
            .addOption(OptionType.CHANNEL, CHANNEL,
                definition.getOptionDescription(CHANNEL),
                true);
        OptionData hours = new OptionData(OptionType.INTEGER, HOURS,
            definition.getOptionDescription(HOURS));
        hours.setMinValue(1);
        hours.setMaxValue(480);
        OptionData startingMessage = new OptionData(OptionType.STRING,
            START_MESSAGE_ID,
            definition.getOptionDescription(START_MESSAGE_ID));
        commandData.addOptions(hours, startingMessage);
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        TextChannel channel = (TextChannel) event
            .getOption(CHANNEL)
            .getAsGuildChannel();

        int hours = event
            .getOption(HOURS, 24l, OptionMapping::getAsLong)
            .intValue();

        Long lastMessageID = event
            .getOption(START_MESSAGE_ID, -1l, OptionMapping::getAsLong);

        BlankUser user = getBlankUserService().getUser(event);
        reply(createLists(new LinkedHashMap<>(), true, channel, user)[0]);

        setLongRunningTask(updateMessages -> retrieveMessages(channel,
            event.getUser().getIdLong(), hours, lastMessageID,
            updateMessages));
    }

    protected void retrieveMessages(TextChannel channel, long userId, int hours,
        Long lastMessageID, Consumer<FormattingData[]> updateMessages) {
        BlankUser user = getBlankUserService()
            .getUser(userId, channel.getGuild().getIdLong());
        LinkedHashMap<String, Integer> messageCounts = new LinkedHashMap<>();

        long lastUpdate = System.currentTimeMillis();

        MessageHistory history;
        List<Message> historyList;
        long epochSecond = System.currentTimeMillis() / 1000;
        if (lastMessageID > 0) {
            history = MessageHistory
                .getHistoryBefore(channel, String.valueOf(lastMessageID))
                .limit(50)
                .complete();
            historyList = history.getRetrievedHistory();
            epochSecond = TimeUtil
                .getTimeCreated(lastMessageID)
                .toEpochSecond();
        } else {
            history = channel.getHistory();
            historyList = history.retrievePast(50).complete();
        }

        long cutoffTime = epochSecond - (hours * 60 * 60);

        historyList = filterMessages(historyList, cutoffTime);

        do {
            historyList
                .stream()
                .map(Message::getAuthor)
                .map(User::getAsMention)
                .forEach(name -> {
                    if (messageCounts.containsKey(name)) {
                        messageCounts
                            .put(name, messageCounts.get(name) + 1);
                    } else {
                        messageCounts.put(name, 1);
                    }
                });

            if (System.currentTimeMillis() - lastUpdate >= 4000L) {
                updateMessages
                    .accept(createLists(messageCounts, true, channel,
                        user));
                lastUpdate = System.currentTimeMillis();
            }

            historyList = filterMessages(history.retrievePast(50).complete(),
                cutoffTime);
        } while (!historyList.isEmpty());
        updateMessages.accept(createLists(messageCounts, false, channel, user));
    }

    private List<Message> filterMessages(List<Message> messages,
        long cutoffTime) {
        return messages.stream().filter(timeFilter(cutoffTime)).toList();
    }

    private Predicate<Message> timeFilter(long cutoffTimeSeconds) {
        return message -> message
            .getTimeCreated()
            .toEpochSecond() > cutoffTimeSeconds;
    }

    protected FormattingData[] createLists(
        LinkedHashMap<String, Integer> messageCounts, boolean pending,
        TextChannel channel, BlankUser sender) {
        FormattingData[] lists = new FormattingData[(int) Math
            .max(1, Math.ceil(messageCounts.size() / 16d))];

        Iterator<Entry<String, Integer>> entryIterator = messageCounts
            .entrySet()
            .stream()
            .sorted(Comparator
                .comparing(Entry<String, Integer>::getValue)
                .reversed())
            .iterator();

        String pendingMarker = getMessageService()
            .format(getBlankUserService()
                .createFormattingData(sender,
                    UtilityMessageType.CHAT_SUMMARY_PENDING)
                .build());

        for (int i = 0; i < lists.length; i++) {
            String body = Stream
                .generate(
                    () -> entryIterator.hasNext() ? entryIterator.next()
                        : null)
                .limit(16)
                .filter(Objects::nonNull)
                .map(entry -> getBlankUserService()
                    .createFormattingData(sender,
                        UtilityMessageType.CHAT_SUMMARY_ENTRY)
                    .dataPairing(GenericFormatDataKey.USER,
                        entry.getKey())
                    .dataPairing(UtilityFormatDataKey.MESSAGE_COUNT,
                        entry.getValue())
                    .build())
                .map(getMessageService()::format)
                .collect(Collectors.joining("\n"));

            lists[i] = getBlankUserService()
                .createFormattingData(sender,
                    UtilityMessageType.CHAT_SUMMARY_LIST)
                .dataPairing(UtilityFormatDataKey.CHANNEL,
                    channel.getName())
                .dataPairing(UtilityFormatDataKey.CHANNEL_MENTION,
                    channel.getAsMention())
                .dataPairing(UtilityFormatDataKey.PAGE, i + 1)
                .dataPairing(UtilityFormatDataKey.CHAT_SUMMARY_BODY, body)
                .dataPairing(UtilityFormatDataKey.PENDING_MARKER,
                    pending ? pendingMarker : "")
                .build();
        }
        return lists;
    }

}
