package com.blank.humanity.discordbot.commands.voting;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.blank.humanity.discordbot.commands.CommandUnitTest;
import com.blank.humanity.discordbot.commands.voting.messages.VotingMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;
import com.blank.humanity.discordbot.services.VotingService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

class VoteCampaignCommandTest extends CommandUnitTest<VoteCampaignCommand> {

    @Mock
    private VotingService votingService;

    protected VoteCampaignCommandTest() {
        super(VoteCampaignCommand.class);
    }

    @BeforeEach
    void setupVotingService() {
        commandMock.setVotingService(votingService);
    }

    @Override
    protected void testCreateCommandData(SlashCommandData commandData) {
        assertThat(commandData.getSubcommands())
            .hasSize(6)
            .anyMatch(hasSubcommand("create", hasOption("name", STRING, true),
                hasOption("description", STRING, true)))
            .anyMatch(
                hasSubcommand("addchoice", hasOption("campaign", STRING, true),
                    hasOption("choice", STRING, true)))
            .anyMatch(hasSubcommand("removechoice",
                hasOption("campaign", STRING, true),
                hasOption("choice", STRING, true)))
            .anyMatch(
                hasSubcommand("start", hasOption("campaign", STRING, true)))
            .anyMatch(
                hasSubcommand("stop", hasOption("campaign", STRING, true)))
            .anyMatch(hasSubcommand("list", hasOption("page", INTEGER, false)));
    }

    private MessageEmbed[] executeStandardSubcommand(BlankUser user,
        String subcommand, String campaign, String choice) {
        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "campaign", campaign),
            optionMapping(OptionType.STRING, "choice", choice));
        doReturn(subcommand).when(event).getSubcommandName();

        return callCommand(event);
    }

    @Test
    void testCreateVoteCampaign(@Mock BlankUser user) {
        String name = "TEST_CAMPAIGN";
        String description = "TEST_DESCRIPTION";

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_CREATED);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_CREATED,
            "%(voteCampaignName)");

        when(votingService.votingCampaignExists(name)).thenReturn(false);
        when(votingService.createVotingCampaign(name, description))
            .thenReturn(new VotingCampaign(2, name.toLowerCase(), description,
                false, Collections.emptyList()));

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "name", name),
            optionMapping(OptionType.STRING, "description", description));
        doReturn("create").when(event).getSubcommandName();

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(name.toLowerCase()));

        verify(votingService).createVotingCampaign(name, description);
        verify(commandService).updateCommand("vote");
    }

    @Test
    void testCreateVoteCampaignExistsAlready(@Mock BlankUser user) {
        String name = "TEST_CAMPAIGN";
        String description = "TEST_DESCRIPTION";

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_EXISTS_ALREADY);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_EXISTS_ALREADY,
            "%(voteCampaignName)");

        when(votingService.votingCampaignExists(name)).thenReturn(true);

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "name", name),
            optionMapping(OptionType.STRING, "description", description));
        doReturn("create").when(event).getSubcommandName();

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(name.toLowerCase()));

        verify(votingService, never()).createVotingCampaign(name, description);
        verify(commandService, never()).updateCommand("vote");
    }

    @Test
    void testAddChoice(@Mock BlankUser user) {
        String campaignName = "test_campaign";
        String choice = "test-choice";

        VotingCampaign campaign = new VotingCampaign();
        campaign.setName(campaignName);

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_CHOICE_ADDED);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_CHOICE_ADDED,
            "%(voteCampaignName)%(voteChoice)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.of(campaign));

        MessageEmbed[] embeds = executeStandardSubcommand(user, "addchoice",
            campaignName, choice);

        assertThat(embeds)
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName + choice));

        assertThat(campaign.getChoices())
            .hasSize(1)
            .doesNotContainNull()
            .anyMatch(voteChoice -> voteChoice.getValue().equals(choice));
        verify(commandService).updateCommand("vote");
    }

    @Test
    void testAddChoiceMissingCampaign(@Mock BlankUser user) {
        String campaignName = "test_campaign";
        String choice = "test-choice";

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND,
            "%(voteCampaignName)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.empty());

        MessageEmbed[] embeds = executeStandardSubcommand(user, "addchoice",
            campaignName, choice);

        assertThat(embeds)
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName));

        verify(commandService, never()).updateCommand("vote");
    }

    @Test
    void testRemoveChoice(@Mock BlankUser user) {
        String campaignName = "test_campaign";
        String choice = "test-choice";

        VotingCampaign campaign = new VotingCampaign();
        campaign.setName(campaignName);
        campaign.addChoice(choice);

        assertThat(campaign.getChoices()).hasSize(1);

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_CHOICE_REMOVED);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_CHOICE_REMOVED,
            "%(voteCampaignName)%(voteChoice)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.of(campaign));

        MessageEmbed[] embeds = executeStandardSubcommand(user, "removechoice",
            campaignName, choice);

        assertThat(embeds)
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName + choice));

        assertThat(campaign.getChoices())
            .isEmpty();
        verify(commandService).updateCommand("vote");
    }

    @Test
    void testRemoveChoiceMissingChoice(@Mock BlankUser user) {
        String campaignName = "test_campaign";
        String choice = "test-choice";

        VotingCampaign campaign = new VotingCampaign();
        campaign.setName(campaignName);

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_CHOICE_NOT_FOUND);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_CHOICE_NOT_FOUND,
            "%(voteCampaignName)%(voteChoice)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.of(campaign));

        MessageEmbed[] embeds = executeStandardSubcommand(user, "removechoice",
            campaignName, choice);

        assertThat(embeds)
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName + choice));

        assertThat(campaign.getChoices())
            .isEmpty();
        verify(commandService, never()).updateCommand("vote");
    }

    @Test
    void testRemoveChoiceMissingCampaign(@Mock BlankUser user) {
        String campaignName = "test_campaign";
        String choice = "test-choice";

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND,
            "%(voteCampaignName)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.empty());

        MessageEmbed[] embeds = executeStandardSubcommand(user, "removechoice",
            campaignName, choice);

        assertThat(embeds)
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName));

        verify(commandService, never()).updateCommand("vote");
    }

    @Test
    void testStartCampaign(@Mock BlankUser user) {
        String campaignName = "test_campaign";

        VotingCampaign campaign = new VotingCampaign();
        campaign.setName(campaignName);

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_STARTED);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_STARTED,
            "%(voteCampaignName)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.of(campaign));

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "campaign", campaignName));
        doReturn("start").when(event).getSubcommandName();

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName));

        assertThat(campaign.isRunning()).isTrue();
        verify(commandService).updateCommand("vote");
    }

    @Test
    void testStartCampaignMissingCampaign(@Mock BlankUser user) {
        String campaignName = "test_campaign";

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND,
            "%(voteCampaignName)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.empty());

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "campaign", campaignName));
        doReturn("start").when(event).getSubcommandName();

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName.toLowerCase()));

        verify(commandService, never()).updateCommand("vote");
    }

    @Test
    void testStopCampaign(@Mock BlankUser user) {
        String campaignName = "test_campaign";

        VotingCampaign campaign = new VotingCampaign();
        campaign.setName(campaignName);

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_STOPPED);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_STOPPED,
            "%(voteCampaignName)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.of(campaign));

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "campaign", campaignName));
        doReturn("stop").when(event).getSubcommandName();

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName));

        assertThat(campaign.isRunning()).isFalse();
        verify(commandService).updateCommand("vote");
    }

    @Test
    void testStopCampaignMissingCampaign(@Mock BlankUser user) {
        String campaignName = "test_campaign";

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND,
            "%(voteCampaignName)");

        when(votingService.getVotingCampaign(campaignName))
            .thenReturn(Optional.empty());

        GenericCommandInteractionEvent event = mockCommandEvent(user,
            optionMapping(OptionType.STRING, "campaign", campaignName));
        doReturn("stop").when(event).getSubcommandName();

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(campaignName.toLowerCase()));

        verify(commandService, never()).updateCommand("vote");
    }

    @Test
    void testListCampaigns(@Mock BlankUser user) {
        String campaignName = "test_campaign";

        List<VotingCampaign> campaigns = IntStream
            .range(1, 6)
            .mapToObj(i -> campaignName + i)
            .map(name -> {
                var campaign = new VotingCampaign();
                campaign.setName(name);
                return campaign;
            })
            .toList();
        when(votingService.getVotingCampaigns()).thenReturn(campaigns);

        mockServiceCreateFormatting(user,
            VotingMessageType.VOTE_CAMPAIGN_LIST);

        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_LIST_DESCRIPTION,
            campaigns
                .stream()
                .map(v -> "%(voteCampaignName)")
                .toArray(i -> new String[i]));
        mockMessageFormats(VotingMessageType.VOTE_CAMPAIGN_LIST,
            "%(voteCampaignListBody)");

        GenericCommandInteractionEvent event = mockCommandEvent(user);
        doReturn("list").when(event).getSubcommandName();

        String expectedList = campaigns
            .stream()
            .map(VotingCampaign::getName)
            .collect(Collectors.joining("\n"));

        assertThat(callCommand(event))
            .hasSize(1)
            .anyMatch(embedHasDescription(expectedList));

        verify(commandService, never()).updateCommand("vote");
    }

}
