package com.blank.humanity.discordbot.commands.economy;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.validation.constraints.Min;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class RichestCommand extends AbstractCommand {

    private static final String PAGE = "page";

    @Override
    public String getCommandName() {
        return "richest";
    }

    @Override
    public SlashCommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        OptionData page = new OptionData(OptionType.INTEGER, PAGE,
            definition.getOptionDescription(PAGE));
        page.setMinValue(1);
        commandData
            .addOptions(page);
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        int page = event
            .getOption(PAGE, 1L, OptionMapping::getAsLong)
            .intValue();

        Stream<BlankUser> richestUsers = getBlankUserService()
            .listUsers(Sort.by(Direction.DESC, "balance"), (page - 1))
            .stream();

        String body = zipWithIndex(richestUsers)
            .map(pair -> formatLeaderboardEntry(pair, page))
            .collect(Collectors.joining("\n"));

        FormattingData data = FormattingData
            .builder()
            .messageType(EconomyMessageType.RICHEST_COMMAND)
            .dataPairing(EconomyFormatDataKey.RICHEST_LIST_PAGE, page)
            .dataPairing(EconomyFormatDataKey.RICHEST_COMMAND_BODY, body)
            .build();

        reply(data);
    }

    private String formatLeaderboardEntry(Pair<BlankUser, Integer> userAndIndex,
        int page) {
        return getMessageService()
            .format(getBlankUserService()
                .createFormattingData(userAndIndex.getFirst(),
                    EconomyMessageType.RICHEST_COMMAND_ENTRY)
                .dataPairing(EconomyFormatDataKey.LEADERBOARD_PLACE,
                    getLeaderboardRanking(page, userAndIndex.getSecond()))
                .build());
    }

    private long getLeaderboardRanking(@Min(1) long page, @Min(0) int index) {
        return (page - 1) * getBlankUserService().getUserListPageSize()
            + (index + 1);
    }

    private Stream<Pair<BlankUser, Integer>> zipWithIndex(
        Stream<BlankUser> users) {
        return StreamUtils
            .zip(users, IntStream.iterate(0, i -> i + 1).boxed(), Pair::of);
    }

}
