package com.blank.humanity.discordbot.entities.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Validated
public class UserClaimData {

    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BlankUser user;

    @NotNull
    @Column(nullable = false)
    private ClaimDataType type;

    @Nullable
    @Setter
    private LocalDateTime lastClaimTime;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    @Default
    @Setter
    private int claimStreak = 0;

    public long getMilliSecondsSinceLastClaim() {
        if (getLastClaimTime() == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.MILLIS
            .between(getLastClaimTime(), LocalDateTime.now());
    }

}
