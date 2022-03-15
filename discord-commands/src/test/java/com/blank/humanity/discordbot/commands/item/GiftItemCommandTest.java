package com.blank.humanity.discordbot.commands.item;

import static org.assertj.core.api.Assertions.assertThat;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.items.GiftItemCommand;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class GiftItemCommandTest extends CommandUnitTest<GiftItemCommand> {

    protected GiftItemCommandTest() {
        super(GiftItemCommand.class);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getOptions())
            .hasSize(3)
            .anyMatch(hasOption("user", OptionType.USER, true))
            .anyMatch(hasOption("item", OptionType.STRING, true, true))
            .anyMatch(hasOption("amount", OptionType.INTEGER, false));
    }

}
