package com.blank.humanity.discordbot.commands.games;

import java.util.List;

import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class BlackjackGameCommand extends AbstractGame {

    private String[] cards = new String[] { "A", "2", "3", "4", "5", "6", "7",
        "8", "9", "J", "Q", "K" };

    @Override
    public String getCommandName() {
        return "blackjack";
    }

    @Override
    public CommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        OptionData bet = new OptionData(OptionType.INTEGER, "bet",
            definition.getOptionDescription("bet"), true);
        bet.setMinValue(1);
        bet.setMaxValue(getCommandConfig().getMaxGameBetAmount());
        commandData.addOptions(bet);
        return commandData;
    }

    @Override
    protected DiscordMenu onGameStart(GenericCommandInteractionEvent event,
        BlankUser user, GameMetadata metadata) {

        return createMenu(user.getDiscordId(), false, metadata.getId());
    }

    private DiscordMenu createMenu(Long discordUser,
        boolean doubleDownDisabled, long gameId) {
        Button doubleDown = Button
            .secondary("doubleDown", "Double Down")
            .withDisabled(doubleDownDisabled);

        return componentMenu()
            .allowedDiscordIds(List.of(discordUser))
            .button("Hit", BlackjackAction.HIT.name(), ButtonStyle.PRIMARY)
            .button("Stand", BlackjackAction.STAND.name(), ButtonStyle.PRIMARY)
            .button(doubleDown, BlackjackAction.DOUBLE_DOWN.name())
            .timeoutTask(() -> finish(gameId))
            .build();
    }

    @Override
    protected DiscordMenu onGameContinue(BlankUser user, GameMetadata metadata,
        Object argument) {
        // TODO Auto-generated method stub
        return null;
    }

}

enum BlackjackAction {
    HIT, STAND, DOUBLE_DOWN;
}