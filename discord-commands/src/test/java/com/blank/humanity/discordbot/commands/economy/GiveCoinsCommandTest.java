package com.blank.humanity.discordbot.commands.economy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class GiveCoinsCommandTest extends CommandUnitTest {

    protected GiveCoinsCommandTest() {
        super(GiveCoinsCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions())
            .hasSize(2)
            .anySatisfy(option -> {
                assertThat(option.getType())
                    .isEqualTo(OptionType.USER);
                assertThat(option.getName()).isEqualTo("user");
                assertThat(option.isRequired()).isTrue();
            })
            .anySatisfy(option -> {
                assertThat(option.getType()).isEqualTo(OptionType.INTEGER);
                assertThat(option.getName()).isEqualTo("amount");
                assertThat(option.isRequired()).isTrue();
            });
    }

    @Test
    void testGiveCoins(@Mock BlankUser user, @Mock Member member) {
        int amount = 122;

        when(blankUserService.getUser(member)).thenReturn(user);

        mockServiceCreateFormatting(user,
            EconomyMessageType.GIVE_COINS_COMMAND);

        mockMessageFormats(EconomyMessageType.GIVE_COINS_COMMAND,
            "COINS_AMOUNT: %(rewardAmount)");

        var event = mockCommandEvent(
            optionMapping(OptionType.USER, "user", member),
            optionMapping(OptionType.INTEGER, "amount", amount));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embed -> embed
                .getDescription()
                .equals("COINS_AMOUNT: " + amount));

        verify(blankUserService).increaseUserBalance(user, amount);
    }

}
