package com.blank.humanity.discordbot.services;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.database.VotingCampaignDao;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.voting.Vote;
import com.blank.humanity.discordbot.entities.voting.VoteChoice;
import com.blank.humanity.discordbot.entities.voting.VotingCampaign;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Service
public class VotingService {

    @Autowired
    private VotingCampaignDao votingCampaignDao;

    @Transactional
    public List<SubcommandData> createVoteSubcommands() {
        return votingCampaignDao
            .findByIsRunning(true)
            .stream()
            .map(this::subcommandFromVotingCampaign)
            .toList();
    }

    @Transactional
    private SubcommandData subcommandFromVotingCampaign(
        VotingCampaign votingCampaign) {
        SubcommandData subcommandData = new SubcommandData(
            votingCampaign.getName(), votingCampaign.getDescription());

        OptionData choiceData = new OptionData(OptionType.STRING, "choice",
            "Your Voting Choice", true);

        votingCampaign
            .getChoices()
            .stream()
            .map(VoteChoice::getValue)
            .forEach(str -> choiceData.addChoice(str, str));

        subcommandData.addOptions(choiceData);

        return subcommandData;
    }

    public boolean votingCampaignExists(String campaign) {
        return votingCampaignDao.existsByName(campaign.toLowerCase());
    }

    @Transactional
    public VotingCampaign createVotingCampaign(String campaign,
        String description) {
        VotingCampaign votingCampaign = new VotingCampaign();
        votingCampaign.setName(campaign.toLowerCase());
        votingCampaign.setDescription(description);
        return votingCampaignDao.save(votingCampaign);
    }

    @Transactional
    public Optional<VotingCampaign> getVotingCampaign(String campaign) {
        return votingCampaignDao.findByName(campaign.toLowerCase());
    }

    public boolean hasUserVoted(VotingCampaign campaign, BlankUser user) {
        return campaign
            .getChoices()
            .stream()
            .flatMap(choice -> choice.getVotes().stream())
            .anyMatch((Vote vote) -> vote.getUserId() == user.getId());
    }

    @Transactional
    public void vote(BlankUser user, VotingCampaign campaign, String choice) {
        VoteChoice voteChoice = campaign
            .getChoices()
            .stream()
            .filter(vote -> vote.getValue().equalsIgnoreCase(choice))
            .findAny()
            .orElseThrow();
        voteChoice.getVotes().add(new Vote(voteChoice, user.getId()));
    }

    public List<VotingCampaign> getVotingCampaigns() {
        return votingCampaignDao.findAll();
    }

}
