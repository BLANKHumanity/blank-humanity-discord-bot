package de.zorro909.blank.BlankDiscordBot.commands.utilities;

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
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeUtil;

@Component
public class ChannelChatSummary extends AbstractCommand {

    public ChannelChatSummary() {
	super("chatsummary");
    }

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData
		.addOption(OptionType.CHANNEL, "channel",
			getCommandDefinition().getOptionDescription("channel"),
			true);
	OptionData hours = new OptionData(OptionType.INTEGER, "hours",
		getCommandDefinition().getOptionDescription("hours"));
	hours.setMinValue(1);
	hours.setMaxValue(480);
	OptionData startingMessage = new OptionData(OptionType.STRING,
		"startmessageid",
		getCommandDefinition().getOptionDescription("startmessageid"));
	commandData.addOptions(hours, startingMessage);
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	TextChannel channel = (TextChannel) event
		.getOption("channel")
		.getAsGuildChannel();

	int hours = Optional
		.ofNullable(event.getOption("hours"))
		.map(OptionMapping::getAsLong)
		.orElse(24l)
		.intValue();

	Long lastMessageID = Optional
		.ofNullable(event.getOption("startmessageid"))
		.map(OptionMapping::getAsString)
		.map(Long::valueOf)
		.orElse(-1l);

	BlankUser user = getBlankUserService().getUser(event);
	reply(event,
		createLists(new LinkedHashMap<>(), true, channel, user)[0]);

	addLongRunningTask(event,
		updateMessages -> retrieveMessages(channel,
			event.getUser().getIdLong(), hours, lastMessageID,
			updateMessages));
    }

    protected void retrieveMessages(TextChannel channel, long userId, int hours,
	    Long lastMessageID, Consumer<FormattingData[]> updateMessages) {
	BlankUser user = getBlankUserService()
		.getUser(userId, channel.getGuild().getIdLong());
	LinkedHashMap<String, Integer> messageCounts = new LinkedHashMap<String, Integer>();

	long lastUpdate = System.currentTimeMillis();

	MessageHistory history;
	List<Message> historyList;
	if (lastMessageID > 0) {
	    history = MessageHistory
		    .getHistoryBefore(channel, String.valueOf(lastMessageID))
		    .limit(50)
		    .complete();
	    historyList = history.getRetrievedHistory();
	} else {
	    history = channel.getHistory();
	    historyList = history.retrievePast(50).complete();
	}

	long cutoffTime = TimeUtil.getTimeCreated(lastMessageID).toEpochSecond()
		- (hours * 60 * 60);

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
		.max(1, Math.nextUp(messageCounts.size() / 16d))];

	Iterator<Entry<String, Integer>> entryIterator = messageCounts
		.entrySet()
		.iterator();

	String pendingMarker = format(getBlankUserService()
		.createFormattingData(sender, MessageType.CHAT_SUMMARY_PENDING)
		.build());

	for (int i = 0; i < lists.length; i++) {
	    String body = Stream
		    .generate(
			    () -> entryIterator.hasNext() ? entryIterator.next()
				    : null)
		    .limit(16)
		    .filter(Objects::nonNull)
		    .map((entry) -> getBlankUserService()
			    .createFormattingData(sender,
				    MessageType.CHAT_SUMMARY_ENTRY)
			    .dataPairing(FormatDataKey.USER, entry.getKey())
			    .dataPairing(FormatDataKey.MESSAGE_COUNT,
				    entry.getValue())
			    .build())
		    .map(this::format)
		    .collect(Collectors.joining("\n"));

	    lists[i] = getBlankUserService()
		    .createFormattingData(sender, MessageType.CHAT_SUMMARY_LIST)
		    .dataPairing(FormatDataKey.CHANNEL, channel.getName())
		    .dataPairing(FormatDataKey.CHANNEL_MENTION,
			    channel.getAsMention())
		    .dataPairing(FormatDataKey.PAGE, i + 1)
		    .dataPairing(FormatDataKey.CHAT_SUMMARY_BODY, body)
		    .dataPairing(FormatDataKey.PENDING_MARKER,
			    pending ? pendingMarker : "")
		    .build();
	}
	return lists;
    }

}
