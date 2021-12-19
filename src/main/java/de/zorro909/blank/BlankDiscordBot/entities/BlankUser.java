package de.zorro909.blank.BlankDiscordBot.entities;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Validated
@NoArgsConstructor
public class BlankUser {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(unique = true, nullable = false)
    private Long discordId;

    @NotNull
    private boolean migrated = false;

    @NotNull
    @Valid
    @Min(value = 0)
    private int balance = 0;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @ElementCollection
    @OneToMany
    private Map<ClaimDataType, UserClaimData> claims = new HashMap<>();

}
