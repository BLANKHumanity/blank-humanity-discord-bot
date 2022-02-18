package com.blank.humanity.discordbot.entities.game;

import java.io.IOException;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.exceptions.game.GameMetadataIOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Column(name = "game", length = 32)
    private String game;

    @NotNull
    private LocalDateTime lastPlayed;

    @Nullable
    @Column(name = "metadata", length = 4096)
    private String metadata;

    @NotNull
    boolean gameFinished = false;

    public <T> T getMetadata(Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(getMetadata(), clazz);
        } catch (IOException exception) {
            throw new GameMetadataIOException(
                "Reading GameMetadata for Game '" + game
                    + "' with ID '" + id + "' has failed",
                exception);
        }
    }

    public void clearMetadata() {
        this.metadata = null;
    }

    public <T> void setMetadata(T gameMetadata) {
        try {
            this.metadata = new ObjectMapper().writeValueAsString(gameMetadata);
        } catch (JsonProcessingException exception) {
            throw new GameMetadataIOException(
                "Writing GameMetadata for Game '" + game
                    + "' with ID '" + id + "' has failed",
                exception);
        }
    }

}
