package de.zorro909.blank.BlankDiscordBot.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.zorro909.blank.BlankDiscordBot.entities.voting.VotingCampaign;

@Repository
public interface VotingCampaignDao
	extends JpaRepository<VotingCampaign, Integer> {

    public Optional<VotingCampaign> findByName(String name);
    
    public List<VotingCampaign> findByIsRunning(boolean running);
    
    public boolean existsByName(String name);
}
