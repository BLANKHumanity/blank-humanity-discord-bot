package de.zorro909.blank.BlankDiscordBot.entities.voting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @NotBlank
    private String description;

    @NotNull
    private boolean isRunning = false;

    @NotNull
    @OneToMany(mappedBy = "votingCampaign", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<VoteChoice> choices = new ArrayList<VoteChoice>();

    public void addChoice(String choice) {
	VoteChoice voteChoice = new VoteChoice();
	voteChoice.setVotingCampaign(this);
	voteChoice.setValue(choice);
	choices.add(voteChoice);
    }

    @Transactional
    public boolean removeChoice(String choice) {
	Optional<VoteChoice> foundChoice = choices
		.stream()
		.filter(voteChoice -> voteChoice
			.getValue()
			.equalsIgnoreCase(choice))
		.findAny();
	if (foundChoice.isEmpty()) {
	    return false;
	}
	choices.remove(foundChoice.get());
	return true;
    }

}
