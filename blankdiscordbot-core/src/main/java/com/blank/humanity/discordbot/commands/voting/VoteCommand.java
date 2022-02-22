package com.blank.humanity.discordbot.commands.voting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.commands.voting.messages.VotingFormatDataKey;
import com.blank.humanity.discordbot.commands.voting.messages.VotingMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;
import com.blank.humanity.discordbot.services.VotingService;

import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class VoteCommand extends AbstractHiddenCommand {

    @Override
    protected String getCommandName() {
        return "vote";
    }

    @Autowired
    private VotingService votingService;

    @Override
    protected SlashCommandData createCommandData(SlashCommandData commandData) {
        commandData.addSubcommands(votingService.createVoteSubcommands());
        return commandData;
    }

    @Override
    protected void onCommand(SlashCommandInteraction event) {
        BlankUser user = getBlankUserService().getUser(event);

        VotingCampaign campaign = votingService
            .getVotingCampaign(event.getSubcommandName())
            .get();

        String choice = event.getOption("choice").getAsString();

        if (votingService.hasUserVoted(campaign, user)) {
            reply(event, getBlankUserService()
                .createFormattingData(user,
                    VotingMessageType.VOTE_COMMAND_ALREADY_VOTED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign.getName())
                .build());
            return;
        }

        votingService.vote(user, campaign, choice);

        reply(event,
            getBlankUserService()
                .createFormattingData(user,
                    VotingMessageType.VOTE_COMMAND_VOTED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign.getName())
                .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
                .build());
    }

}
