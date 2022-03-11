package com.blank.humanity.discordbot.commands.voting;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.commands.voting.messages.VotingFormatDataKey;
import com.blank.humanity.discordbot.commands.voting.messages.VotingMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;
import com.blank.humanity.discordbot.services.VotingService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Component
public class VoteCampaignCommand extends AbstractHiddenCommand {

    @Override
    public String getCommandName() {
        return "votecampaign";
    }

    @Autowired
    private VotingService votingService;

    @Override
    public SlashCommandData createCommandData(
        SlashCommandData commandData,
        CommandDefinition definition) {
        SubcommandData create = new SubcommandData("create",
            definition.getOptionDescription("create"));
        create
            .addOption(OptionType.STRING, "name",
                definition.getOptionDescription("name"),
                true);
        create
            .addOption(OptionType.STRING, "description",
                definition
                    .getOptionDescription("description"),
                true);

        SubcommandData addChoice = new SubcommandData("addchoice",
            definition.getOptionDescription("addchoice"));
        addChoice
            .addOption(OptionType.STRING, "campaign",
                definition.getOptionDescription("campaign"),
                true);
        addChoice
            .addOption(OptionType.STRING, "choice",
                definition.getOptionDescription("choice"),
                true);

        SubcommandData removeChoice = new SubcommandData("removechoice",
            definition.getOptionDescription("removechoice"));
        removeChoice
            .addOption(OptionType.STRING, "campaign",
                definition.getOptionDescription("campaign"),
                true);
        removeChoice
            .addOption(OptionType.STRING, "choice",
                definition.getOptionDescription("choice"),
                true);

        SubcommandData start = new SubcommandData("start",
            definition.getOptionDescription("start"));
        start
            .addOption(OptionType.STRING, "campaign",
                definition.getOptionDescription("campaign"),
                true);

        SubcommandData stop = new SubcommandData("stop",
            definition.getOptionDescription("stop"));
        stop
            .addOption(OptionType.STRING, "campaign",
                definition.getOptionDescription("campaign"),
                true);

        SubcommandData list = new SubcommandData("list",
            definition.getOptionDescription("list"));
        list
            .addOption(OptionType.INTEGER, "page",
                definition.getOptionDescription("page"));

        commandData
            .addSubcommands(create, addChoice, removeChoice, start, stop,
                list);

        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        switch (event.getSubcommandName()) {
        case "create" -> create(event);
        case "addchoice" -> addChoice(event);
        case "removechoice" -> removeChoice(event);
        case "start" -> start(event);
        case "stop" -> stop(event);
        case "list" -> list(event);
        default -> throw new RuntimeException("Unknown Subcommand");
        }
    }

    private void create(GenericCommandInteractionEvent event) {
        BlankUser user = getBlankUserService().getUser(event);
        String campaign = event.getOption("name").getAsString();
        String description = event.getOption("description").getAsString();

        if (votingService.votingCampaignExists(campaign)) {
            reply(getBlankUserService()
                .createFormattingData(user,
                    VotingMessageType.VOTE_CAMPAIGN_EXISTS_ALREADY)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
            return;
        }

        VotingCampaign votingCampaign = votingService
            .createVotingCampaign(campaign, description);

        getCommandService().updateCommand(getCommandName());

        reply(getBlankUserService()
            .createFormattingData(user,
                VotingMessageType.VOTE_CAMPAIGN_CREATED)
            .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                votingCampaign.getName())
            .build());
    }

    private void addChoice(GenericCommandInteractionEvent event) {
        String campaign = event.getOption("campaign").getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            votingCampaign.get().setRunning(true);
            getCommandService().updateCommand(getCommandName());

            String choice = event.getOption("choice").getAsString();
            votingCampaign.get().addChoice(choice);

            getCommandService().updateCommand(getCommandName());

            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_CHOICE_ADDED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        }
    }

    private void removeChoice(GenericCommandInteractionEvent event) {
        String campaign = event.getOption("campaign").getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            votingCampaign.get().setRunning(true);
            getCommandService().updateCommand(getCommandName());

            String choice = event.getOption("choice").getAsString();
            if (!votingCampaign.get().removeChoice(choice)) {
                reply(getBlankUserService()
                    .createFormattingData(
                        getBlankUserService().getUser(event),
                        VotingMessageType.VOTE_CAMPAIGN_CHOICE_NOT_FOUND)
                    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                        campaign)
                    .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
                    .build());
                return;
            }

            getCommandService().updateCommand(getCommandName());

            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_CHOICE_REMOVED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        }
    }

    private void start(GenericCommandInteractionEvent event) {
        String campaign = event.getOption("campaign").getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            votingCampaign.get().setRunning(true);
            getCommandService().updateCommand(getCommandName());
            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_STARTED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        }
    }

    private void stop(GenericCommandInteractionEvent event) {
        String campaign = event.getOption("campaign").getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            votingCampaign.get().setRunning(false);
            getCommandService().updateCommand(getCommandName());
            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_STOPPED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getBlankUserService().getUser(event),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        }
    }

    private void list(GenericCommandInteractionEvent event) {
        String body = votingService
            .getVotingCampaigns()
            .stream()
            .map(this::formatVotingCampaign)
            .collect(Collectors.joining("\n"));

        reply(getBlankUserService()
            .createFormattingData(getUser(),
                VotingMessageType.VOTE_CAMPAIGN_LIST)
            .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_LIST_BODY, body)
            .build());
    }

    private String formatVotingCampaign(VotingCampaign campaign) {
        return getMessageService()
            .format(FormattingData
                .builder()
                .messageType(VotingMessageType.VOTE_CAMPAIGN_LIST_DESCRIPTION)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign.getName())
                .build());
    }

}
