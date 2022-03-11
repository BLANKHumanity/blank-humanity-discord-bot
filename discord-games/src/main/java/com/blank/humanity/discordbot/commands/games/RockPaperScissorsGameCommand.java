package com.blank.humanity.discordbot.commands.games;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.games.messages.GameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GameMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class RockPaperScissorsGameCommand extends AbstractGame {

    @Autowired
    private SecureRandom random;

    private static final String CHOICE = "choice";

    private char[][] resultMap = { { 't', 'w', 'l' }, { 'l', 't', 'w' },
        { 'w', 'l', 't' } };

    @Override
    public String getCommandName() {
        return "rps";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        OptionData betAmount = new OptionData(OptionType.INTEGER, "bet",
            definition.getOptionDescription("bet"), true);
        betAmount.setMinValue(1);
        betAmount.setMaxValue(getCommandConfig().getMaxGameBetAmount());
        OptionData selection = new OptionData(OptionType.STRING, CHOICE,
            definition.getOptionDescription(CHOICE), true);
        selection.addChoice("🇷", "rock");
        selection.addChoice("🇵", "paper");
        selection.addChoice("🇸", "scissors");
        commandData.addOptions(betAmount, selection);
        return commandData;
    }

    @Override
    protected DiscordMenu onGameStart(GenericCommandInteractionEvent event,
        BlankUser user,
        GameMetadata metadata) {
        int betAmount = (int) event.getOption("bet").getAsLong();
        if (betAmount > user.getBalance()) {
            reply(getBlankUserService()
                .createFormattingData(user,
                    GameMessageType.GAME_BET_NOT_ENOUGH_MONEY)
                .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
                .build());
            abort(metadata);
        } else {

            int opponentRoll = random.nextInt(3);

            int userSelection = selectionToInt(
                event.getOption(CHOICE).getAsString());

            char result = resultMap[opponentRoll][userSelection];

            switch (result) {
            case 't' -> tie(betAmount, userSelection, opponentRoll);
            case 'w' -> win(betAmount, userSelection, opponentRoll);
            case 'l' -> loss(betAmount, userSelection, opponentRoll);
            default -> throw new IllegalArgumentException(
                "RPS Result can only be 't', 'w', or 'l', but it is '" + result
                    + "'!");
            }

            finish(metadata);
        }
        return null;
    }

    private int selectionToInt(String option) {
        return switch (option) {
        case "rock" -> 0;
        case "paper" -> 1;
        case "scissors" -> 2;
        default -> throw new IllegalArgumentException(
            "Unexpected value: " + option);
        };
    }

    private String intToSelection(int selection) {
        return switch (selection) {
        case 0 -> "🪨";
        case 1 -> "📰";
        case 2 -> "✂";
        default -> throw new IllegalArgumentException(
            "Unexpected value: " + selection);
        };
    }

    private void loss(int betAmount, int userSel, int botSel) {
        getBlankUserService().decreaseUserBalance(getUser(), betAmount);

        FormattingData result = getBlankUserService()
            .createFormattingData(getUser(),
                GameMessageType.ROCK_PAPER_SCISSORS_LOSS)
            .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
            .dataPairing(GameFormatDataKey.RPS_USER,
                intToSelection(userSel))
            .dataPairing(GameFormatDataKey.RPS_BOT, intToSelection(botSel))
            .build();

        reply(result);
    }

    private void win(int betAmount, int userSel, int botSel) {
        int reward = calculateWinnings(betAmount);
        getBlankUserService()
            .increaseUserBalance(getUser(), reward - betAmount);

        FormattingData result = getBlankUserService()
            .createFormattingData(getUser(),
                GameMessageType.ROCK_PAPER_SCISSORS_WIN)
            .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
            .dataPairing(GameFormatDataKey.REWARD_AMOUNT, reward)
            .dataPairing(GameFormatDataKey.RPS_USER,
                intToSelection(userSel))
            .dataPairing(GameFormatDataKey.RPS_BOT, intToSelection(botSel))
            .build();

        reply(result);
    }

    private void tie(int betAmount, int userSel, int botSel) {
        FormattingData result = getBlankUserService()
            .createFormattingData(getUser(),
                GameMessageType.ROCK_PAPER_SCISSORS_TIE)
            .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
            .dataPairing(GameFormatDataKey.RPS_USER,
                intToSelection(userSel))
            .dataPairing(GameFormatDataKey.RPS_BOT,
                intToSelection(botSel))
            .build();

        reply(result);
    }

    @Override
    protected DiscordMenu onGameContinue(BlankUser user, GameMetadata metadata,
        Object argument) {
        return null;
    }

}
