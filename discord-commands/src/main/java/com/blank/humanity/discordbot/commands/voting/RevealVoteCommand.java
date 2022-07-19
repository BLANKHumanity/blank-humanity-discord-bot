package com.blank.humanity.discordbot.commands.voting;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.blank.humanity.discordbot.aop.Argument;
import com.blank.humanity.discordbot.aop.DiscordCommand;
import com.blank.humanity.discordbot.commands.AbstractCommand;
import com.blank.humanity.discordbot.commands.voting.messages.VotingFormatDataKey;
import com.blank.humanity.discordbot.commands.voting.messages.VotingMessageType;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.voting.VoteChoice;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;
import com.blank.humanity.discordbot.services.VotingService;
import com.blank.humanity.discordbot.utils.FormattingData;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

@DiscordCommand("revealvote")
@Argument(name = "campaign")
public class RevealVoteCommand extends AbstractCommand {

    @Autowired
    private VotingService votingService;

    @Override
    public String getCommandName() {
        return super.getCommandName();
    }

    @Override
    protected void onCommand(GenericCommandInteractionEvent event) {
        BlankUser user = getUser();

        String campaign = event.getOption("campaign").getAsString();

        Optional<VotingCampaign> votingCampaign = votingService
            .getVotingCampaign(campaign);
        Optional<String> choiceBody = votingCampaign
            .map(VotingCampaign::getChoices)
            .map(list -> list
                .stream()
                .map(this::formatVotes)
                .collect(Collectors.joining("\n")));

        if (votingCampaign.isEmpty() || choiceBody.isEmpty()) {
            reply(getBlankUserService()
                .createFormattingData(user,
                    VotingMessageType.VOTE_CAMPAIGN_NOT_FOUND)
                .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME,
                    campaign)
                .build());
            return;
        }
        reply(getBlankUserService()
            .createFormattingData(user,
                VotingMessageType.VOTE_CAMPAIGN_VOTE_DISPLAY_HEADER)
            .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_NAME, campaign)
            .dataPairing(
                VotingFormatDataKey.VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY,
                choiceBody.get())
            .dataPairing(VotingFormatDataKey.VOTE_CAMPAIGN_DESCRIPTION,
                votingCampaign.get().getDescription())
            .build());
    }

    private String formatVotes(VoteChoice choice) {
        return getMessageService()
            .format(FormattingData
                .builder()
                .messageType(
                    VotingMessageType.VOTE_CAMPAIGN_VOTE_CHOICE_DISPLAY)
                .dataPairing(VotingFormatDataKey.VOTE_CHOICE, choice.getValue())
                .dataPairing(VotingFormatDataKey.VOTE_COUNT,
                    choice.getVoteCount())
                .build());
    }

}
