package com.blank.humanity.discordbot.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.CustomFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.CommandService;
import com.blank.humanity.discordbot.services.MenuService;
import com.blank.humanity.discordbot.services.MessageService;
import com.blank.humanity.discordbot.services.TransactionExecutor;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.NamedFormatter;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.data.DataObject;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public abstract class CommandUnitTest<C extends AbstractCommand> {

    private Map<MessageType, Integer> messageIndex = new HashMap<>();

    private Map<MessageType, String[]> messageFormats = new HashMap<>();

    private Class<C> commandClass;

    protected C commandMock;

    @Mock
    protected JDA jda;

    @Mock
    protected BlankUserService blankUserService;

    @Mock
    protected TransactionExecutor transactionExecutor;

    @Mock
    protected MenuService menuService;

    @Mock(lenient = true)
    protected MessageService messageService;

    @Mock
    protected CommandService commandService;

    protected CommandUnitTest(Class<C> commandClass) {
        this.commandClass = commandClass;
    }

    @BeforeEach
    void setup() {
        commandMock = mock(commandClass,
            withSettings().defaultAnswer(CALLS_REAL_METHODS));
        commandMock
            .setJda(jda)
            .setBlankUserService(blankUserService)
            .setTransactionExecutor(transactionExecutor)
            .setMenuService(menuService)
            .setMessageService(messageService)
            .setCommandService(commandService);

        when(messageService.format(any(FormattingData.class)))
            .then(this::formatMockedInvocation);
        when(messageService.format(any(FormattingData[].class)))
            .then(invocation -> Stream
                .of(invocation.getArgument(0, FormattingData[].class))
                .map(messageService::format)
                .toArray(i -> new String[i]));
    }

    @AfterEach
    void clearUp() {
        this.commandMock.clearThreadLocals();
    }

    @Test
    void startTestCreateCommandData(@Mock CommandDefinition definition) {
        SlashCommandData commandData = Commands.slash("test", "test");

        lenient()
            .when(definition.getOptionDescription(anyString()))
            .thenReturn("TEST_DESCRIPTION");

        prepareCreateCommandData(commandData, definition);
        commandData = (SlashCommandData) commandMock
            .createCommandData(commandData, definition);
        testCreateCommandData(commandData);
    }

    private String formatMockedInvocation(InvocationOnMock invocation) {
        FormattingData data = invocation
            .getArgument(0, FormattingData.class);
        int index = messageIndex
            .compute(data.messageType(),
                (key, val) -> val == null ? 0 : val + 1);
        assertThat(messageFormats.get(data.messageType()))
            .as("MessageType %s has not been mocked for Formatting",
                data
                    .messageType()
                    .toString())
            .isNotNull()
            .as("MessageType %s has been formatted more than the available Mocks allow (current: %d",
                data.messageType().toString(), index)
            .hasSizeGreaterThan(index);
        return NamedFormatter
            .namedFormat(messageFormats.get(data.messageType())[index],
                data.getDataPairings());
    }

    /**
     * Prepare test execution of
     * {@linkplain AbstractCommand#createCommandData(SlashCommandData, com.blank.humanity.discordbot.config.commands.CommandDefinition)}
     * 
     * @param commandData {@linkplain SlashCommandData}
     * @param definition  Mock of {@linkplain CommandDefinition}
     */
    protected void prepareCreateCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
    }

    /**
     * Test correct interaction with the mock SlashCommandData.
     * {@linkplain AbstractCommand#createCommandData(SlashCommandData, CommandDefinition)}
     * has been executed already.
     * 
     * @param commandData {@linkplain SlashCommandData}
     */
    protected abstract void testCreateCommandData(SlashCommandData commandData);

    protected void mockMessageFormats(MessageType messageType,
        String... messages) {
        messageFormats.put(messageType, messages);
    }

    protected GenericCommandInteractionEvent mockCommandEvent(
        OptionMapping... arguments) {
        return mockCommandEvent(mock(BlankUser.class), arguments);
    }

    protected GenericCommandInteractionEvent mockCommandEvent(BlankUser user,
        OptionMapping... arguments) {
        return mockCommandEvent(user, mock(Member.class), arguments);
    }

    protected GenericCommandInteractionEvent mockCommandEvent(BlankUser user,
        Member member, OptionMapping... arguments) {
        GenericCommandInteractionEvent event = mock(
            GenericCommandInteractionEvent.class,
            withSettings().defaultAnswer(CALLS_REAL_METHODS).lenient());

        doReturn(member).when(event).getMember();
        doReturn(List.of(arguments)).when(event).getOptions();

        commandMock.setMember(member);
        commandMock.setUser(user);

        return event;
    }

    protected MessageEmbed[] callCommand(GenericCommandInteractionEvent event) {
        commandMock.setCommandEvent(event);
        commandMock.onCommand(event);
        assertThat(commandMock.getUnsentReply())
            .as("Command has not issued any Reply")
            .isNotNull()
            .hasAtLeastOneElementOfType(MessageEmbed.class);
        return commandMock.getUnsentReply();
    }

    protected CommandAutoCompleteInteractionEvent mockAutocompleteEvent(
        OptionMapping... arguments) {
        return mockAutocompleteEvent(mock(BlankUser.class), arguments);
    }

    protected CommandAutoCompleteInteractionEvent mockAutocompleteEvent(
        BlankUser user, OptionMapping... arguments) {
        return mockAutocompleteEvent(user, mock(Member.class), arguments);
    }

    protected CommandAutoCompleteInteractionEvent mockAutocompleteEvent(
        BlankUser user, Member member, OptionMapping... arguments) {
        CommandAutoCompleteInteractionEvent event = mock(
            CommandAutoCompleteInteractionEvent.class,
            withSettings().defaultAnswer(CALLS_REAL_METHODS).lenient());

        doReturn(member).when(event).getMember();
        doReturn(List.of(arguments)).when(event).getOptions();

        commandMock.setMember(member);
        commandMock.setUser(user);

        return event;
    }

    protected Collection<Command.Choice> callAutocomplete(
        CommandAutoCompleteInteractionEvent event) {
        commandMock.setAutoCompleteEvent(event);
        return commandMock.onAutoComplete(event);
    }

    protected void executeLongRunningTask() {
//        assertThat(commandMock.lo)
    }

    protected void mockServiceCreateFormatting(BlankUser user,
        MessageType type) {
        when(blankUserService.createFormattingData(user, type))
            .then(invocation -> mockedCreateFormattingData(invocation, user,
                type));
    }

    private FormattingData.FormattingDataBuilder mockedCreateFormattingData(
        InvocationOnMock invocation, BlankUser user, MessageType type) {
        FormattingData.FormattingDataBuilder builder = FormattingData
            .builder()
            .dataPairing(
                CustomFormatDataKey.key("balance"), user.getBalance())
            .messageType(type);
        String username = blankUserService.getUsername(user);
        if (username != null) {
            builder
                .dataPairing(GenericFormatDataKey.USER,
                    blankUserService.getUsername(user))
                .dataPairing(GenericFormatDataKey.USER_MENTION,
                    blankUserService.getUsername(user));
        }
        return builder;
    }

    @SuppressWarnings("unchecked")
    protected OptionMapping optionMapping(OptionType type, String name,
        Object resolvedValue) {
        DataObject object = DataObject.empty();
        object.put("type", type.getKey());
        object.put("name", name);
        if (type == OptionType.USER || type == OptionType.MENTIONABLE) {
            object.put("value", 12);
        } else {
            object.put("value", resolvedValue.toString());
        }

        TLongObjectMap<Object> objectMap = mock(TLongObjectMap.class);
        lenient().doReturn(resolvedValue).when(objectMap).get(any(Long.class));

        return new OptionMapping(object, objectMap);
    }

    @SafeVarargs
    protected final Predicate<SubcommandData> hasSubcommand(String name,
        Predicate<OptionData>... optionPredicates) {
        return subcommandData -> subcommandData.getName().equals(name)
            && subcommandData
                .getOptions()
                .stream()
                .allMatch(option -> Stream
                    .of(optionPredicates)
                    .anyMatch(predicate -> predicate.test(option)));
    }

    protected Predicate<OptionData> hasOption(String name, OptionType type) {
        return option -> option.getName().equals(name)
            && option.getType() == type;
    }

    protected Predicate<OptionData> hasOption(String name,
        OptionType type, boolean required) {
        return option -> option.getName().equals(name)
            && option.getType() == type && option.isRequired() == required;
    }

    protected Predicate<OptionData> hasOption(String name, OptionType type,
        boolean required, boolean autocomplete) {
        return option -> option.getName().equals(name)
            && option.getType() == type && option.isRequired() == required
            && option.isAutoComplete() == autocomplete;
    }

    protected Predicate<MessageEmbed> embedHasDescription(String name) {
        return embed -> {
            if (embed.getDescription().equals(name)) {
                return true;
            }
            throw new AssertionError(
                "Expected embed to have following description: '" + name
                    + "', but got: '" + embed.getDescription() + "'!");
        };
    }

}
