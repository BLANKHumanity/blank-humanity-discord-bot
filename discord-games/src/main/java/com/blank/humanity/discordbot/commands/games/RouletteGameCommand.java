package com.blank.humanity.discordbot.commands.games;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.games.messages.GameFormatDataKey;
import com.blank.humanity.discordbot.commands.games.messages.GameMessageType;
import com.blank.humanity.discordbot.commands.games.messages.GenericGameFormatDataKey;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.MessageType;
import com.blank.humanity.discordbot.entities.game.GameMetadata;
import com.blank.humanity.discordbot.entities.game.RouletteMetadata;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.exceptions.economy.NotEnoughBalanceException;
import com.blank.humanity.discordbot.utils.FormattingData;
import com.blank.humanity.discordbot.utils.menu.DiscordMenu;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

@Component
@Slf4j
public class RouletteGameCommand extends AbstractGame {

    @Autowired
    private SecureRandom random;

    @Override
    public String getCommandName() {
        return "roulette";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        OptionData betAmount = new OptionData(OptionType.INTEGER, "bet",
            definition.getOptionDescription("bet"), true);
        betAmount.setMinValue(1);
        betAmount.setMaxValue(getCommandConfig().getMaxGameBetAmount());
        commandData.addOptions(betAmount);
        return commandData;
    }

    @Override
    protected DiscordMenu onGameStart(GenericCommandInteractionEvent event,
        BlankUser user,
        GameMetadata metadata) {
        int betAmount = (int) event.getOption("bet").getAsLong();

        RouletteMetadata roulette = new RouletteMetadata(betAmount, 1,
            Collections.emptyList());

        metadata.setMetadata(roulette);

        FormattingData reply = playGame(metadata);

        reply(reply);

        log.info("Is Game Finished: " + metadata.isGameFinished());
        if (!metadata.isGameFinished()) {
            return createMenu(metadata);
        } else {
            return null;
        }
    }

    @Override
    protected DiscordMenu onGameContinue(BlankUser user, GameMetadata metadata,
        Object argument) {
        if (argument instanceof List) {
            reply(rejectNewCommand(user));
        } else if (argument.equals("STOP")) {
            finish(metadata);
        } else {
            reply(playGame(metadata));
        }
        return null;
    }

    private FormattingData rejectNewCommand(BlankUser user) {
        return getBlankUserService()
            .createFormattingData(user,
                GameMessageType.ROULETTE_GAME_RUNNING)
            .build();
    }

    private DiscordMenu createMenu(GameMetadata metadata) {
        return componentMenu()
            .allowedDiscordIds(List.of(metadata.getUser().getDiscordId()))
            .timeoutTask(() -> finish(metadata.getId()))
            .button("STOP", "STOP", ButtonStyle.DANGER)
            .button("CONTINUE", "CONTINUE", ButtonStyle.PRIMARY)
            .build();
    }

    private FormattingData playGame(GameMetadata metadata) {
        RouletteMetadata roulette;
        roulette = metadata.getMetadata(RouletteMetadata.class);
        BlankUser user = metadata.getUser();

        int betAmount = roulette.getBetAmount();

        try {
            getBlankUserService().decreaseUserBalance(user, betAmount);
        } catch (NotEnoughBalanceException e) {
            if (roulette.getRound() == 1) {
                abort(metadata);
            } else {
                finish(metadata);
            }
            return getBlankUserService()
                .createFormattingData(user,
                    GameMessageType.GAME_BET_NOT_ENOUGH_MONEY)
                .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
                .dataPairing(GenericGameFormatDataKey.GAME_NAME,
                    getGameDefinition().getDisplayName())
                .build();
        }

        int randomNumber = random.nextInt(6);
        boolean success = randomNumber != 0;

        int reward = calculateWinnings(betAmount);

        if (success) {
            getBlankUserService().increaseUserBalance(user, reward);
            roulette.setBetAmount(reward);
            roulette.setRound(roulette.getRound() + 1);

            return buildRouletteMessage(metadata, roulette, user, betAmount,
                reward, GameMessageType.ROULETTE_WIN_MESSAGE);
        } else {
            finish(metadata);
            return buildRouletteMessage(metadata, roulette, user, betAmount,
                reward, GameMessageType.ROULETTE_LOSE_MESSAGE);
        }
    }

    private FormattingData buildRouletteMessage(GameMetadata metadata,
        RouletteMetadata roulette, BlankUser user, int betAmount,
        int reward, MessageType resultMessage) {
        List<Integer> previousBetAmounts = new ArrayList<>();
        if (roulette.getPreviousBetAmounts() != null) {
            previousBetAmounts = roulette.getPreviousBetAmounts();
        }
        previousBetAmounts.add(betAmount);

        String rouletteHeader = previousBetAmounts
            .stream()
            .map(bet -> getMessageService()
                .format(getBlankUserService()
                    .createFormattingData(user,
                        GameMessageType.ROULETTE_BET_AND_PULL)
                    .dataPairing(GameFormatDataKey.BET_AMOUNT, bet)
                    .build()))
            .collect(Collectors.joining("\n"));

        roulette.setPreviousBetAmounts(previousBetAmounts);

        metadata.setMetadata(roulette);

        String rouletteResult = getMessageService()
            .format(getBlankUserService()
                .createFormattingData(user, resultMessage)
                .dataPairing(GameFormatDataKey.REWARD_AMOUNT, reward)
                .dataPairing(GameFormatDataKey.BET_AMOUNT, betAmount)
                .build());

        return getBlankUserService()
            .createFormattingData(user, GameMessageType.ROULETTE_DISPLAY)
            .dataPairing(GameFormatDataKey.ROULETTE_HEADER, rouletteHeader)
            .dataPairing(GameFormatDataKey.ROULETTE_RESULT, rouletteResult)
            .build();
    }

}
