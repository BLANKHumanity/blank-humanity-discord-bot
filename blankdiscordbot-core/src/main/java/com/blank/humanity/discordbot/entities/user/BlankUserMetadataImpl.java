package com.blank.humanity.discordbot.entities.user;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(value = MetadataId.class)
@Table(name = "BLANK_USER_METADATA")
public class BlankUserMetadataImpl implements BlankUserMetadata {

    @Id
    @NonNull
    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BlankUser user;

    @Id
    @NonNull
    @Getter
    @Column(nullable = false)
    private String metadataKey;

    @Nullable
    @Setter
    @Column(nullable = true)
    private String value;

    @Override
    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}
