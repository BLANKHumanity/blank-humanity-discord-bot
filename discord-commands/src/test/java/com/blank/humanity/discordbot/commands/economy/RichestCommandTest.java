package com.blank.humanity.discordbot.commands.economy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class RichestCommandTest extends CommandUnitTest<RichestCommand> {

    protected RichestCommandTest() {
        super(RichestCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions())
            .hasSize(1)
            .anyMatch(hasOption("page", OptionType.INTEGER, false));
    }

    @Test
    void testRichestFirstPage() {
        GenericCommandInteractionEvent event = mockCommandEvent();

        int amountUsers = 3;
        List<BlankUser> users = mockUsers(amountUsers);

        mockMessageFormats(EconomyMessageType.RICHEST_COMMAND,
            "%(richestCommandBody)PAGE: %(richestListPage)");

        when(blankUserService.listUsers(Sort.by(Direction.DESC, "balance"), 0))
            .thenReturn(new PageImpl<BlankUser>(users));

        MessageEmbed[] embeds = callCommand(event);

        String expectedBody = users
            .stream()
            .map(user -> blankUserService.getUsername(user) + ":"
                + user.getBalance() + ":" + user.getId())
            .collect(Collectors.joining("\n")) + "PAGE: 1";

        assertThat(embeds)
            .hasSize(1)
            .anyMatch(embedHasDescription(expectedBody));
    }

    @Test
    void testRichestThirdPage() {
        GenericCommandInteractionEvent event = mockCommandEvent(
            optionMapping(OptionType.INTEGER, "page", 3));

        int amountUsers = 5;
        List<BlankUser> users = mockUsers(amountUsers);

        mockMessageFormats(EconomyMessageType.RICHEST_COMMAND,
            "%(richestCommandBody)PAGE: %(richestListPage)");

        when(blankUserService.listUsers(Sort.by(Direction.DESC, "balance"), 2))
            .thenReturn(new PageImpl<BlankUser>(users));

        MessageEmbed[] embeds = callCommand(event);

        String expectedBody = users
            .stream()
            .map(user -> blankUserService.getUsername(user) + ":"
                + user.getBalance() + ":" + user.getId())
            .collect(Collectors.joining("\n")) + "PAGE: 3";

        assertThat(embeds)
            .hasSize(1)
            .anyMatch(embedHasDescription(expectedBody));
    }

    private List<BlankUser> mockUsers(int amountUsers) {
        List<BlankUser> users = IntStream
            .range(0, amountUsers)
            .boxed()
            .sorted(Collections.reverseOrder())
            .map(index -> {
                BlankUser user = new BlankUser();
                user.setId((long) (amountUsers - index));
                user.setBalance(index * 1000);
                mockServiceCreateFormatting(user,
                    EconomyMessageType.RICHEST_COMMAND_ENTRY);
                when(blankUserService.getUsername(user))
                    .thenReturn("user" + index);
                return user;
            })
            .toList();

        String[] messageFormats = IntStream
            .range(0, amountUsers)
            .mapToObj(i -> "%(user):%(balance):%(leaderboardPlace)")
            .toArray(i -> new String[i]);

        mockMessageFormats(EconomyMessageType.RICHEST_COMMAND_ENTRY,
            messageFormats);
        return users;
    }

}
