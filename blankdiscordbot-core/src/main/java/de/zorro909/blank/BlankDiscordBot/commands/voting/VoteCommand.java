package de.zorro909.blank.BlankDiscordBot.commands.voting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.zorro909.blank.BlankDiscordBot.commands.AbstractHiddenCommand;
import de.zorro909.blank.BlankDiscordBot.config.messages.MessageType;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.voting.VotingCampaign;
import de.zorro909.blank.BlankDiscordBot.services.VotingService;
import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Component
public class VoteCommand extends AbstractHiddenCommand {

    public VoteCommand() {
	super("vote");
    }

    @Autowired
    private VotingService votingService;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	commandData.addSubcommands(votingService.createVoteSubcommands());
	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	BlankUser user = getBlankUserService().getUser(event);

	VotingCampaign campaign = votingService
		.getVotingCampaign(event.getSubcommandName())
		.get();

	String choice = event.getOption("choice").getAsString();

	if (votingService.hasUserVoted(campaign, user)) {
	    reply(event,
		    getBlankUserService()
			    .createFormattingData(user,
				    MessageType.VOTE_COMMAND_ALREADY_VOTED)
			    .dataPairing(FormatDataKey.VOTE_CAMPAIGN_NAME,
				    campaign.getName())
			    .build());
	    return;
	}

	votingService.vote(user, campaign, choice);

	reply(event, getBlankUserService()
		.createFormattingData(user, MessageType.VOTE_COMMAND_VOTED)
		.dataPairing(FormatDataKey.VOTE_CAMPAIGN_NAME,
			campaign.getName())
		.dataPairing(FormatDataKey.VOTE_CHOICE, choice)
		.build());
    }

}
