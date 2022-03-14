package com.blank.humanity.discordbot.commands.economy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.ClaimDataType;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class DailyCommandTest extends CommandUnitTest<DailyCommand> {

    protected DailyCommandTest() {
        super(DailyCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions()).isEmpty();
    }

    @Test
    void testDailyCommandFirstClaim(@Mock BlankUser user) {
        String testFormat = "TestFormat";

        when(blankUserService.claimReward(user, ClaimDataType.DAILY_CLAIM))
            .thenReturn(FormattingData
                .builder()
                .success(true));

        mockMessageFormats(EconomyMessageType.DAILY_COMMAND_MESSAGE,
            testFormat);

        GenericCommandInteractionEvent event = mockCommandEvent(user);

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed.getDescription().equals(testFormat));
    }

    @Test
    void testDailyCommandClaimStreak(@Mock BlankUser user) {
        int claimStreak = 4;
        String testFormat = "ClaimStreak: %(claimStreak)";

        when(blankUserService.claimReward(user, ClaimDataType.DAILY_CLAIM))
            .thenReturn(FormattingData
                .builder()
                .dataPairing(EconomyFormatDataKey.CLAIM_STREAK, claimStreak)
                .success(true));

        mockMessageFormats(EconomyMessageType.DAILY_COMMAND_MESSAGE_STREAK,
            testFormat);

        GenericCommandInteractionEvent event = mockCommandEvent(user);

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed
                .getDescription()
                .equals("ClaimStreak: " + claimStreak));
    }

    @Test
    void testDailyCommandAlreadyClaimed(@Mock BlankUser user) {
        String testFormat = "Already claimed";

        when(blankUserService.claimReward(user, ClaimDataType.DAILY_CLAIM))
            .thenReturn(FormattingData
                .builder()
                .success(false));

        mockMessageFormats(EconomyMessageType.DAILY_COMMAND_ALREADY_CLAIMED_MESSAGE,
            testFormat);

        GenericCommandInteractionEvent event = mockCommandEvent(user);

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed.getDescription().equals(testFormat));
    }

}
