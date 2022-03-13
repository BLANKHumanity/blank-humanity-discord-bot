package com.blank.humanity.discordbot.commands.economy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class BalanceCommandTest extends CommandUnitTest {

    protected BalanceCommandTest() {
        super(BalanceCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions())
            .hasSize(1)
            .anyMatch(hasOption("user", OptionType.USER, false));
    }

    @Test
    void testBalanceCommand(@Mock BlankUser user, @Mock Member member,
        @Mock Guild guild) {
        long guildId = 10;
        long userId = 22;
        int balance = 412;
        when(guild.getIdLong()).thenReturn(guildId);
        when(member.getGuild()).thenReturn(guild);
        when(member.getIdLong()).thenReturn(userId);

        when(blankUserService.getUser(userId, guildId)).thenReturn(user);

        when(user.getBalance()).thenReturn(balance);

        mockServiceCreateFormatting(user,
            EconomyMessageType.BALANCE_COMMAND_MESSAGE);

        mockMessageFormats(EconomyMessageType.BALANCE_COMMAND_MESSAGE,
            "TEST_BALANCE: %(balance)");

        GenericCommandInteractionEvent event = mockCommandEvent(user, member);

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed
                .getDescription()
                .equals("TEST_BALANCE: " + balance));
    }

    @Test
    void testBalanceCommandOtherUser(@Mock BlankUser user, @Mock Member member,
        @Mock Guild guild) {
        long guildId = 53;
        long userId = 74;
        int balance = 12352;
        when(guild.getIdLong()).thenReturn(guildId);
        when(member.getGuild()).thenReturn(guild);
        when(member.getIdLong()).thenReturn(userId);

        when(blankUserService.getUser(userId, guildId)).thenReturn(user);

        when(user.getBalance()).thenReturn(balance);

        mockServiceCreateFormatting(user,
            EconomyMessageType.BALANCE_COMMAND_MESSAGE);

        mockMessageFormats(EconomyMessageType.BALANCE_COMMAND_MESSAGE,
            "TEST_BALANCE: %(balance)");

        GenericCommandInteractionEvent event = mockCommandEvent(
            optionMapping(OptionType.USER, "user", member));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed
                .getDescription()
                .equals("TEST_BALANCE: " + balance));
    }

}
