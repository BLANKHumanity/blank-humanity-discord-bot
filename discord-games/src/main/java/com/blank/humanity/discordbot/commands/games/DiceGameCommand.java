
package com.blank.humanity.discordbot.commands.games;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.games.messages.GameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GameMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.fake.FakeUser;
import com.blank.humanity.discordbot.entities.user.fake.FakeUserType;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class DiceGameCommand extends AbstractGame {

    @Autowired
    private SecureRandom random;

    @Override
    public String getCommandName() {
        return "dice";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
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

            getBlankUserService().decreaseUserBalance(user, betAmount);

            FakeUser diceJackpot = FakeUserType.DICE_JACKPOT
                .getFakeUser(getBlankUserService());

            diceJackpot.increaseBalance((int) Math.round(betAmount / 20d));

            DiceRoll playerRoll = diceRoll();
            DiceRoll opponentRoll = diceRoll();

            MessageType messageType;
            int reward = 0;

            if (playerRoll.sum() > opponentRoll.sum()) {
                messageType = GameMessageType.DICE_GAME_WIN;
                reward = calculateWinnings(betAmount);

                if (playerRoll.isDouble()) {
                    reward *= 1.5;
                }
            } else if (playerRoll.isSnakeEyes()) {
                messageType = GameMessageType.DICE_GAME_JACKPOT;

                int jackpot = Math
                    .min(diceJackpot.getBalance(), betAmount * 100);

                reward = calculateWinnings(betAmount) + jackpot;
                diceJackpot.decreaseBalance(jackpot);
            } else if (playerRoll.sum() == opponentRoll.sum()) {
                messageType = GameMessageType.DICE_GAME_DRAW;
                reward = betAmount;
            } else {
                messageType = GameMessageType.DICE_GAME_LOSS;
            }

            if (reward != 0) {
                getBlankUserService()
                    .increaseUserBalance(user, reward);
            }

            reply(getBlankUserService()
                .createFormattingData(user, messageType)
                .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
                .dataPairing(GameFormatDataKey.REWARD_AMOUNT, reward)
                .dataPairing(GameFormatDataKey.DICE_ROLL_USER,
                    playerRoll.toString())
                .dataPairing(GameFormatDataKey.DICE_ROLL_OPPONENT,
                    opponentRoll.toString())
                .build());
            finish(metadata);
        }
        return null;

    }

    private DiceRoll diceRoll() {
        return new DiceRoll(random.nextInt(6) + 1, random.nextInt(6) + 1);
    }

    @Override
    protected DiscordMenu onGameContinue(BlankUser user, GameMetadata metadata,
        Object argument) {
        return null;
    }

    private record DiceRoll(int roll1, int roll2) {

        @Override
        public String toString() {
            return intToEmoji(roll1) + " " + intToEmoji(roll2);
        }

        public int sum() {
            return roll1 + roll2;
        }

        public boolean isSnakeEyes() {
            return sum() == 2;
        }

        public boolean isDouble() {
            return roll1 == roll2;
        }

        private String intToEmoji(int number) {
            return switch (number) {
            case 1 -> ":one:";
            case 2 -> ":two:";
            case 3 -> ":three:";
            case 4 -> ":four:";
            case 5 -> ":five:";
            case 6 -> ":six:";
            default -> ":interrobang: (Something bad happened)";
            };
        }

    }

}
