package com.blank.humanity.discordbot.commands.voting;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.commands.voting.messages.VotingFormatDataKey;
import com.blank.humanity.discordbot.commands.voting.messages.VotingMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.voting.VoteChoice;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;
import com.blank.humanity.discordbot.services.VotingService;
import com.blank.humanity.discordbot.utils.FormattingData;

import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Component
public class VoteCampaignCommand extends AbstractHiddenCommand {

    private static final String CREATE = "create";
    private static final String ADD_CHOICE = "addchoice";
    private static final String REMOVE_CHOICE = "removechoice";
    private static final String CHOICE = "choice";
    private static final String CAMPAIGN = "campaign";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String LIST = "list";
    private static final String PAGE = "page";
    private static final String START = "start";
    private static final String STOP = "stop";

    @Setter(onMethod = @__({ @Autowired }))
    private VotingService votingService;

    @Override
    public String getCommandName() {
        return "votecampaign";
    }

    @Override
    public SlashCommandData createCommandData(
        SlashCommandData commandData,
        CommandDefinition definition) {
        SubcommandData create = new SubcommandData(CREATE,
            definition.getOptionDescription(CREATE));
        create
            .addOption(OptionType.STRING, NAME,
                definition.getOptionDescription(NAME),
                true);
        create
            .addOption(OptionType.STRING, DESCRIPTION,
                definition
                    .getOptionDescription(DESCRIPTION),
                true);

        SubcommandData addChoice = new SubcommandData(ADD_CHOICE,
            definition.getOptionDescription(ADD_CHOICE));
        addChoice
            .addOption(OptionType.STRING, CAMPAIGN,
                definition.getOptionDescription(CAMPAIGN),
                true, true);
        addChoice
            .addOption(OptionType.STRING, CHOICE,
                definition.getOptionDescription(CHOICE),
                true);

        SubcommandData removeChoice = new SubcommandData(REMOVE_CHOICE,
            definition.getOptionDescription(REMOVE_CHOICE));
        removeChoice
            .addOption(OptionType.STRING, CAMPAIGN,
                definition.getOptionDescription(CAMPAIGN),
                true, true);
        removeChoice
            .addOption(OptionType.STRING, CHOICE,
                definition.getOptionDescription(CHOICE),
                true, true);

        SubcommandData start = new SubcommandData(START,
            definition.getOptionDescription(START));
        start
            .addOption(OptionType.STRING, CAMPAIGN,
                definition.getOptionDescription(CAMPAIGN),
                true, true);

        SubcommandData stop = new SubcommandData(STOP,
            definition.getOptionDescription(STOP));
        stop
            .addOption(OptionType.STRING, CAMPAIGN,
                definition.getOptionDescription(CAMPAIGN),
                true, true);

        SubcommandData list = new SubcommandData(LIST,
            definition.getOptionDescription(LIST));
        list
            .addOption(OptionType.INTEGER, PAGE,
                definition.getOptionDescription(PAGE));

        commandData
            .addSubcommands(create, addChoice, removeChoice, start, stop,
                list);

        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        switch (event.getSubcommandName()) {
        case CREATE -> create(event);
        case ADD_CHOICE -> addChoice(event);
        case REMOVE_CHOICE -> removeChoice(event);
        case START -> start(event);
        case STOP -> stop(event);
        case LIST -> list();
        default -> throw new UnsupportedOperationException(
            "Unknown Subcommand");
        }
    }

    @NonNull
    protected Collection<Command.Choice> onAutoComplete(
        @NonNull CommandAutoCompleteInteractionEvent autoCompleteEvent) {
        return switch (autoCompleteEvent.getFocusedOption().getName()) {
        case CAMPAIGN -> autoCompleteCampaign(autoCompleteEvent);
        case CHOICE -> autoCompleteChoice(autoCompleteEvent);
        default -> Collections.emptyList();
        };
    }

    private Collection<Command.Choice> autoCompleteCampaign(
        @NonNull CommandAutoCompleteInteractionEvent autoCompleteEvent) {
        String campaignName = autoCompleteEvent
            .getFocusedOption()
            .getValue()
            .toLowerCase()
            .replace(" ", "_");

        Stream<VotingCampaign> campaigns = votingService
            .getVotingCampaigns()
            .stream()
            .filter(campaign -> campaign.getName().contains(campaignName));

        if (autoCompleteEvent.getSubcommandName().equalsIgnoreCase(START)) {
            campaigns = campaigns
                .filter(Predicate.not(VotingCampaign::isRunning));
        } else if (autoCompleteEvent
            .getSubcommandName()
            .equalsIgnoreCase(STOP)) {
            campaigns = campaigns
                .filter(VotingCampaign::isRunning);
        }

        return campaigns
            .map(campaign -> new Command.Choice(campaign.getName(),
                campaign.getName()))
            .toList();
    }

    private Collection<Command.Choice> autoCompleteChoice(
        @NonNull CommandAutoCompleteInteractionEvent autoCompleteEvent) {
        Optional<VotingCampaign> campaign = autoCompleteEvent
            .getOption(NAME, mapping -> votingService
                .getVotingCampaign(mapping.getAsString()));

        String userChoice = autoCompleteEvent
            .getFocusedOption()
            .getValue()
            .toLowerCase();

        return campaign
            .map(VotingCampaign::getChoices)
            .stream()
            .flatMap(List::stream)
            .map(VoteChoice::getValue)
            .filter(
                choice -> choice.toLowerCase().contains(userChoice))
            .map(choice -> new Command.Choice(choice, choice))
            .toList();
    }

    private void create(GenericCommandInteractionEvent event) {
        String campaign = event.getOption(NAME).getAsString();
        String description = event.getOption(DESCRIPTION).getAsString();

        if (votingService.votingCampaignExists(campaign)) {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_EXISTS_ALREADY)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign.toLowerCase())
                .build());
            return;
        }

        VotingCampaign votingCampaign = votingService
            .createVotingCampaign(campaign, description);

        getCommandService().updateCommand("vote");

        reply(getBlankUserService()
            .createFormattingData(getUser(),
                VotingMessageType.VOTE_CAMPAIGN_CREATED)
            .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                votingCampaign.getName())
            .build());
    }

    private void addChoice(GenericCommandInteractionEvent event) {
        String campaign = event.getOption(CAMPAIGN).getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            String choice = event.getOption(CHOICE).getAsString();
            votingCampaign.get().addChoice(choice);

            getCommandService().updateCommand("vote");

            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_CHOICE_ADDED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        }
    }

    private void removeChoice(GenericCommandInteractionEvent event) {
        String campaign = event.getOption(CAMPAIGN).getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            String choice = event.getOption(CHOICE).getAsString();
            if (!votingCampaign.get().removeChoice(choice)) {
                reply(getBlankUserService()
                    .createFormattingData(
                        getUser(),
                        VotingMessageType.VOTE_CAMPAIGN_CHOICE_NOT_FOUND)
                    .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                        campaign.toLowerCase())
                    .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
                    .build());
                return;
            }

            getCommandService().updateCommand("vote");

            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_CHOICE_REMOVED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign.toLowerCase())
                .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign.toLowerCase())
                .build());
        }
    }

    private void start(GenericCommandInteractionEvent event) {
        String campaign = event.getOption(CAMPAIGN).getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            votingCampaign.get().setRunning(true);
            getCommandService().updateCommand("vote");
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_STARTED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        }
    }

    private void stop(GenericCommandInteractionEvent event) {
        String campaign = event.getOption(CAMPAIGN).getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);

        if (votingCampaign.isPresent()) {
            votingCampaign.get().setRunning(false);
            getCommandService().updateCommand("vote");
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_STOPPED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        } else {
            reply(getBlankUserService()
                .createFormattingData(getUser(),
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
        }
    }

    private void list() {
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
