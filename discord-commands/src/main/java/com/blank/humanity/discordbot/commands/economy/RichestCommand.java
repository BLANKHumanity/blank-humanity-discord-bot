package com.blank.humanity.discordbot.commands.economy;

import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Min;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyFormatDataKey;
import com.blank.humanity.discordbot.commands.economy.messages.EconomyMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
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
        BlankUser user = getUser();

        List<BlankUser> richestUsers = blankUserService
            .listUsers(Sort.by(Direction.DESC, "balance"), (page - 1))
            .toList();

        StringBuilder body = new StringBuilder();

        for (int i = 0; i < richestUsers.size(); i++) {
            body
                .append(getMessageService()
                    .format(blankUserService
                        .createFormattingData(richestUsers.get(i),
                            EconomyMessageType.RICHEST_COMMAND_ENTRY)
                        .dataPairing(EconomyFormatDataKey.LEADERBOARD_PLACE,
                            getLeaderboardRanking(page, i))
                        .build()));
            body.append("\n");
        }

        FormattingData data = blankUserService
            .createFormattingData(user, EconomyMessageType.RICHEST_COMMAND)
            .dataPairing(EconomyFormatDataKey.RICHEST_LIST_PAGE, page)
            .dataPairing(EconomyFormatDataKey.RICHEST_COMMAND_BODY, body)
            .build();

        reply(data);
    }

    public long getLeaderboardRanking(@Min(1) long page, @Min(0) int index) {
        return (page - 1) * blankUserService.getUserListPageSize()
            + (index + 1);
    }

}
