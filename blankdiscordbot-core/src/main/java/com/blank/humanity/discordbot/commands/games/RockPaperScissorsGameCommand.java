package com.blank.humanity.discordbot.commands.games;

import java.security.SecureRandom;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.games.messages.GameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GameMessageType;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.menu.ReactionMenu;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class RockPaperScissorsGameCommand extends AbstractGame {

    @Autowired
    private SecureRandom random;

    private char[][] resultMap = { { 't', 'w', 'l' }, { 'l', 't', 'w' },
        { 'w', 'l', 't' } };

    @Override
    protected String getCommandName() {
        return "rps";
    }

    @Override
    protected SlashCommandData createCommandData(SlashCommandData commandData) {
        OptionData betAmount = new OptionData(OptionType.INTEGER, "bet",
            getCommandDefinition().getOptionDescription("bet"), true);
        betAmount.setMinValue(1);
        betAmount.setMaxValue(getCommandConfig().getMaxGameBetAmount());
        OptionData selection = new OptionData(OptionType.STRING, "choice",
            getCommandDefinition().getOptionDescription("choice"), true);
        selection.addChoice("🇷", "rock");
        selection.addChoice("🇵", "paper");
        selection.addChoice("🇸", "scissors");
        commandData.addOptions(betAmount, selection);
        return commandData;
    }

    @Override
    protected ReactionMenu onGameStart(SlashCommandInteraction event, BlankUser user,
        GameMetadata metadata) {
        int betAmount = (int) event.getOption("bet").getAsLong();
        if (betAmount > user.getBalance()) {
            reply(event, getBlankUserService()
                .createFormattingData(user,
                    GameMessageType.GAME_BET_NOT_ENOUGH_MONEY)
                .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
                .build());
            abort(metadata);
            return null;
        }

        int opponentRoll = random.nextInt(3);

        int userSelection = selectionToInt(
            event.getOption("choice").getAsString());

        char result = resultMap[opponentRoll][userSelection];

        switch (result) {
        case 't' -> tie(event, user, betAmount, userSelection, opponentRoll);
        case 'w' -> win(event, user, betAmount, userSelection, opponentRoll);
        case 'l' -> loss(event, user, betAmount, userSelection, opponentRoll);
        default -> throw new IllegalArgumentException(
            "RPS Result can only be 't', 'w', or 'l', but it is '" + result
                + "'!");
        }

        finish(metadata);
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

    private void loss(SlashCommandInteraction event, BlankUser user, int betAmount,
        int userSel, int botSel) {
        getBlankUserService().decreaseUserBalance(user, betAmount);

        FormattingData result = getBlankUserService()
            .createFormattingData(user,
                GameMessageType.ROCK_PAPER_SCISSORS_LOSS)
            .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
            .dataPairing(GameFormatDataKey.RPS_USER,
                intToSelection(userSel))
            .dataPairing(GameFormatDataKey.RPS_BOT, intToSelection(botSel))
            .build();

        reply(event, result);
    }

    private void win(SlashCommandInteraction event, BlankUser user, int betAmount,
        int userSel, int botSel) {
            int reward = calculateWinnings(betAmount);
        getBlankUserService()
            .increaseUserBalance(user,
                reward - betAmount);

        FormattingData result = getBlankUserService()
            .createFormattingData(user,
                GameMessageType.ROCK_PAPER_SCISSORS_WIN)
            .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
            .dataPairing(GameFormatDataKey.REWARD_AMOUNT, reward)
            .dataPairing(GameFormatDataKey.RPS_USER,
                intToSelection(userSel))
            .dataPairing(GameFormatDataKey.RPS_BOT, intToSelection(botSel))
            .build();

        reply(event, result);
    }

    private void tie(SlashCommandInteraction event, BlankUser user, int betAmount,
        int userSel, int botSel) {
        FormattingData result = getBlankUserService()
            .createFormattingData(user,
                GameMessageType.ROCK_PAPER_SCISSORS_TIE)
            .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
            .dataPairing(GameFormatDataKey.RPS_USER,
                intToSelection(userSel))
            .dataPairing(GameFormatDataKey.RPS_BOT,
                intToSelection(botSel))
            .build();

        reply(event, result);
    }

    @Override
    protected ReactionMenu onGameContinue(BlankUser user, GameMetadata metadata,
        Object argument, Consumer<FormattingData> messageEdit) {
        return null;
    }

}
