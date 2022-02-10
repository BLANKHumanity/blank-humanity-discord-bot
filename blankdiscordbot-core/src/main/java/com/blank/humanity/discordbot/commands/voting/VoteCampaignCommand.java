package com.blank.humanity.discordbot.commands.voting;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.commands.voting.messages.VotingFormatDataKey;
import com.blank.humanity.discordbot.commands.voting.messages.VotingMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;
import com.blank.humanity.discordbot.services.VotingService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Component
public class VoteCampaignCommand extends AbstractHiddenCommand {

    public VoteCampaignCommand() {
	super("votecampaign");
    }

    @Autowired
    private VotingService votingService;

    @Autowired
    private VoteCommand voteCommand;

    @Override
    protected CommandData createCommandData(CommandData commandData) {
	SubcommandData create = new SubcommandData("create",
		getCommandDefinition().getOptionDescription("create"));
	create
		.addOption(OptionType.STRING, "name",
			getCommandDefinition().getOptionDescription("name"),
			true);
	create
		.addOption(OptionType.STRING, "description",
			getCommandDefinition()
				.getOptionDescription("description"),
			true);

	SubcommandData addChoice = new SubcommandData("addchoice",
		getCommandDefinition().getOptionDescription("addchoice"));
	addChoice
		.addOption(OptionType.STRING, "campaign",
			getCommandDefinition().getOptionDescription("campaign"),
			true);
	addChoice
		.addOption(OptionType.STRING, "choice",
			getCommandDefinition().getOptionDescription("choice"),
			true);

	SubcommandData removeChoice = new SubcommandData("removechoice",
		getCommandDefinition().getOptionDescription("removechoice"));
	removeChoice
		.addOption(OptionType.STRING, "campaign",
			getCommandDefinition().getOptionDescription("campaign"),
			true);
	removeChoice
		.addOption(OptionType.STRING, "choice",
			getCommandDefinition().getOptionDescription("choice"),
			true);

	SubcommandData start = new SubcommandData("start",
		getCommandDefinition().getOptionDescription("start"));
	start
		.addOption(OptionType.STRING, "campaign",
			getCommandDefinition().getOptionDescription("campaign"),
			true);

	SubcommandData stop = new SubcommandData("stop",
		getCommandDefinition().getOptionDescription("stop"));
	stop
		.addOption(OptionType.STRING, "campaign",
			getCommandDefinition().getOptionDescription("campaign"),
			true);

	SubcommandData list = new SubcommandData("list",
		getCommandDefinition().getOptionDescription("list"));
	list
		.addOption(OptionType.INTEGER, "page",
			getCommandDefinition().getOptionDescription("page"));

	commandData
		.addSubcommands(create, addChoice, removeChoice, start, stop,
			list);

	return commandData;
    }

    @Override
    protected void onCommand(SlashCommandEvent event) {
	switch (event.getSubcommandName()) {
	case "create" -> create(event);
	case "addchoice" -> addChoice(event);
	case "removechoice" -> removeChoice(event);
	case "start" -> start(event);
	case "stop" -> stop(event);
	case "list" -> list(event);
	}
    }

    private void create(SlashCommandEvent event) {
	BlankUser user = getBlankUserService().getUser(event);
	String campaign = event.getOption("name").getAsString();
	String description = event.getOption("description").getAsString();

	if (votingService.votingCampaignExists(campaign)) {
	    reply(event, getBlankUserService()
		    .createFormattingData(user,
			    VotingMessageType.VOTE_CAMPAIGN_EXISTS_ALREADY)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .build());
	    return;
	}

	VotingCampaign votingCampaign = votingService
		.createVotingCampaign(campaign, description);

	voteCommand.updateCommandDefinition();

	reply(event,
		getBlankUserService()
			.createFormattingData(user,
				VotingMessageType.VOTE_CAMPAIGN_CREATED)
			.dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
				votingCampaign.getName())
			.build());
    }

    private void addChoice(SlashCommandEvent event) {
	String campaign = event.getOption("campaign").getAsString();

	Optional<VotingCampaign> votingCampaign = votingService
		.getVotingCampaign(campaign);

	if (votingCampaign.isPresent()) {
	    votingCampaign.get().setRunning(true);
	    voteCommand.updateCommandDefinition();

	    String choice = event.getOption("choice").getAsString();
	    votingCampaign.get().addChoice(choice);

	    voteCommand.updateCommandDefinition();

	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_CHOICE_ADDED)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
		    .build());
	} else {
	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .build());
	}
    }

    private void removeChoice(SlashCommandEvent event) {
	String campaign = event.getOption("campaign").getAsString();

	Optional<VotingCampaign> votingCampaign = votingService
		.getVotingCampaign(campaign);

	if (votingCampaign.isPresent()) {
	    votingCampaign.get().setRunning(true);
	    voteCommand.updateCommandDefinition();

	    String choice = event.getOption("choice").getAsString();
	    if (!votingCampaign.get().removeChoice(choice)) {
		reply(event, getBlankUserService()
			.createFormattingData(
				getBlankUserService().getUser(event),
				VotingMessageType.VOTE_CAMPAIGN_CHOICE_NOT_FOUND)
			.dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
				campaign)
			.dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
			.build());
		return;
	    }

	    voteCommand.updateCommandDefinition();

	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_CHOICE_REMOVED)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
		    .build());
	} else {
	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .build());
	}
    }

    private void start(SlashCommandEvent event) {
	String campaign = event.getOption("campaign").getAsString();

	Optional<VotingCampaign> votingCampaign = votingService
		.getVotingCampaign(campaign);

	if (votingCampaign.isPresent()) {
	    votingCampaign.get().setRunning(true);
	    voteCommand.updateCommandDefinition();
	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_STARTED)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .build());
	} else {
	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .build());
	}
    }

    private void stop(SlashCommandEvent event) {
	String campaign = event.getOption("campaign").getAsString();

	Optional<VotingCampaign> votingCampaign = votingService
		.getVotingCampaign(campaign);

	if (votingCampaign.isPresent()) {
	    votingCampaign.get().setRunning(false);
	    voteCommand.updateCommandDefinition();
	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_STOPPED)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .build());
	} else {
	    reply(event, getBlankUserService()
		    .createFormattingData(getBlankUserService().getUser(event),
			    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
		    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			    campaign)
		    .build());
	}
    }

    private void list(SlashCommandEvent event) {
	BlankUser user = getBlankUserService().getUser(event);
	String body = votingService
		.getVotingCampaigns()
		.stream()
		.map(this::formatVotingCampaign)
		.collect(Collectors.joining("\n"));

	reply(event, getBlankUserService()
		.createFormattingData(user,
			VotingMessageType.VOTE_CAMPAIGN_LIST)
		.dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_LIST_BODY, body)
		.build());
    }

    private String formatVotingCampaign(VotingCampaign campaign) {
	return format(FormattingData
		.builder()
		.messageType(VotingMessageType.VOTE_CAMPAIGN_LIST_DESCRIPTION)
		.dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
			campaign.getName())
		.build());
    }

}
