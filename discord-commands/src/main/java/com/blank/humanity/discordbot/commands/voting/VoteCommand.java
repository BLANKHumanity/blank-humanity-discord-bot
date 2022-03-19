package com.blank.humanity.discordbot.commands.voting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blank.humanity.discordbot.commands.AbstractHiddenCommand;
import com.blank.humanity.discordbot.commands.voting.messages.VotingFormatDataKey;
import com.blank.humanity.discordbot.commands.voting.messages.VotingMessageType;
import com.blank.humanity.discordbot.config.commands.CommandDefinition;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;
import com.blank.humanity.discordbot.services.VotingService;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class VoteCommand extends AbstractHiddenCommand {

    @Autowired
    private VotingService votingService;

    @Override
    public String getCommandName() {
        return "vote";
    }

    @Override
    public CommandData createCommandData(SlashCommandData commandData,
        CommandDefinition definition) {
        commandData.addSubcommands(votingService.createVoteSubcommands());
        return commandData;
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        BlankUser user = getBlankUserService().getUser(event);

        VotingCampaign campaign = votingService
            .getVotingCampaign(event.getSubcommandName())
            .orElseThrow();

        String choice = event.getOption("choice").getAsString();

        if (votingService.hasUserVoted(campaign, user)) {
            reply(getBlankUserService()
                .createFormattingData(user,
                    VotingMessageType.VOTE_COMMAND_ALREADY_VOTED)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign.getName())
                .build());
            return;
        }

        votingService.vote(user, campaign, choice);

        reply(getBlankUserService()
            .createFormattingData(user,
                VotingMessageType.VOTE_COMMAND_VOTED)
            .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                campaign.getName())
            .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice)
            .build());
    }

}
