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
import com.blank.humanity.discordbot.config.messages.GenericFormatDataKey;
import com.blank.humanity.discordbot.config.messages.GenericMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class RichestCommand extends AbstractCommand {

    @Override
    protected String getCommandName() {
        return "richest";
    }

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData
		.addOption(OptionType.INTEGER, "page",
			getCommandDefinition().getOptionDescription("page"));
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	long page = Optional
		.ofNullable(event.getOption("page"))
		.map(OptionMapping::getAsLong)
		.orElse(1L);
	BlankUser user = blankUserService.getUser(event);

	if (page < 1) {
	    reply(event,
		    blankUserService
			    .createFormattingData(user,
				    GenericMessageType.ERROR_MESSAGE)
			    .dataPairing(GenericFormatDataKey.ERROR_MESSAGE,
				    "Page needs to be bigger than 0")
			    .build());
	    return;
	}

	List<BlankUser> richestUsers = blankUserService
		.listUsers(Sort.by(Direction.DESC, "balance"), (int) (page - 1))
		.toList();

	String body = "";

	for (int i = 0; i < richestUsers.size(); i++) {
	    body += format(blankUserService
		    .createFormattingData(richestUsers.get(i),
			    EconomyMessageType.RICHEST_COMMAND_ENTRY)
		    .dataPairing(EconomyFormatDataKey.LEADERBOARD_PLACE,
			    getLeaderboardRanking(page, i))
		    .build());
	    body += "\n";
	}

	FormattingData data = blankUserService
		.createFormattingData(user, EconomyMessageType.RICHEST_COMMAND)
		.dataPairing(EconomyFormatDataKey.RICHEST_LIST_PAGE, page)
		.dataPairing(EconomyFormatDataKey.RICHEST_COMMAND_BODY, body)
		.build();

	reply(event, data);
    }

    public long getLeaderboardRanking(@Min(1) long page, @Min(0) int index) {
	return (page - 1) * blankUserService.getUserListPageSize()
		+ (index + 1);
    }

}
