package com.blank.humanity.discordbot.commands.economy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.ClaimDataType;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class WorkCommandTest extends CommandUnitTest<WorkCommand> {

    protected WorkCommandTest() {
        super(WorkCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions()).isEmpty();
    }

    @Test
    void testDailyCommandFirstClaim(@Mock BlankUser user) {
        String testFormat = "TestFormat";

        when(blankUserService.claimReward(user, ClaimDataType.WORK_CLAIM))
            .thenReturn(FormattingData
                .builder()
                .success(true));

        mockMessageFormats(EconomyMessageType.WORK_COMMAND_MESSAGE,
            testFormat);

        GenericCommandInteractionEvent event = mockCommandEvent(user);

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed.getDescription().equals(testFormat));
    }

    @Test
    void testDailyCommandAlreadyClaimed(@Mock BlankUser user) {
        String testFormat = "Already claimed";

        when(blankUserService.claimReward(user, ClaimDataType.WORK_CLAIM))
            .thenReturn(FormattingData
                .builder()
                .success(false));

        mockMessageFormats(
            EconomyMessageType.WORK_COMMAND_ALREADY_CLAIMED_MESSAGE,
            testFormat);

        GenericCommandInteractionEvent event = mockCommandEvent(user);

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed.getDescription().equals(testFormat));
    }

}
