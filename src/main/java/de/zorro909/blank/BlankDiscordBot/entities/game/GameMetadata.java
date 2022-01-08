package de.zorro909.blank.BlankDiscordBot.entities.game;

import java.io.IOException;
import java.sql.Clob;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import lombok.Data;

@Entity
@Data
public class GameMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Lazy
    @NotNull
    @OneToOne
    private BlankUser user;

    @NotNull
    private GameType game;

    @NotNull
    private LocalDateTime lastPlayed;

    @Nullable
    private String metadataClassname;

    @Nullable
    @Column(name = "metadata", length = 4096)
    private String metadata;

    @NotNull
    boolean gameFinished = false;

    public <T> T getMetadata(Class<T> clazz) throws IOException {
	if (!clazz.getName().equalsIgnoreCase(metadataClassname)) {
	    throw new RuntimeException("Wrong metadata Class requested!");
	}
	return new ObjectMapper().readValue(getMetadata(), clazz);
    }

    public void clearMetadata() {
	this.metadata = null;
    }

    public <T> void setMetadata(T gameMetadata) throws JsonProcessingException {
	if (!gameMetadata
		.getClass()
		.getName()
		.equalsIgnoreCase(metadataClassname)) {
	    throw new RuntimeException("Wrong metadata Class provided!");
	}
	this.metadata = new ObjectMapper().writeValueAsString(gameMetadata);
    }

}
