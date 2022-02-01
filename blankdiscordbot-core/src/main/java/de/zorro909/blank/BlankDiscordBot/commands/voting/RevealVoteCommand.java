package de.zorro909.blank.BlankDiscordBot.commands.voting;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.voting.VoteChoice;
import de.zorro909.blank.BlankDiscordBot.entities.voting.VotingCampaign;
import de.zorro909.blank.BlankDiscordBot.services.VotingService;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import de.zorro909.blank.BlankDiscordBot.utils.FormattingData;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class RevealVoteCommand extends AbstractCommand {

    public RevealVoteCommand() {
	super("revealvote");
    }

    @Autowired
    private VotingService votingService;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData
		.addOption(OptionType.STRING, "campaign", "The VoteCampaign",
			true);
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = getBlankUserService().getUser(event);

	String campaign = event.getOption("campaign").getAsString();

	Optional<VotingCampaign> votingCampaign = votingService
		.getVotingCampaign(campaign);
	Optional<String> choiceBody = votingCampaign
		.map(VotingCampaign::getChoices)
		.map(list -> list
			.stream()
			.map(this::formatVotes)
			.collect(Collectors.joining("\n")));

	if (choiceBody.isEmpty()) {
	    reply(event, getBlankUserService()
		    .createFormattingData(user,
			    MessageType.VOTE_CAMPAIGN_NOT_FOUND)
		    .dataPairing(FormatDataKey.VOTE_CAMPAIGN_NAME, campaign)
		    .build());
	    return;
	}
	reply(event, getBlankUserService()
		.createFormattingData(user,
			MessageType.VOTE_CAMPAIGN_VOTE_DISPLAY_HEADER)
		.dataPairing(FormatDataKey.VOTE_CAMPAIGN_NAME, campaign)
		.dataPairing(FormatDataKey.VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY,
			choiceBody.get())
		.dataPairing(FormatDataKey.VOTE_CAMPAIGN_DESCRIPTION,
			votingCampaign.get().getDescription())
		.build());
    }

    private String formatVotes(VoteChoice choice) {
	return format(FormattingData
		.builder()
		.messageType(MessageType.VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY)
		.dataPairing(FormatDataKey.VOTE_CHOICE, choice.getValue())
		.dataPairing(FormatDataKey.VOTE_COUNT, choice.getVoteCount())
		.build());
    }

}
