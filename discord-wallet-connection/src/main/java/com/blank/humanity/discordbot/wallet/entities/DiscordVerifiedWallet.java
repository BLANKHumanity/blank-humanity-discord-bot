package com.blank.humanity.discordbot.wallet.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Positive;

import com.blank.humanity.discordbot.entities.user.BlankUser;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
public class DiscordVerifiedWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false, referencedColumnName = "id")
    private BlankUser user;

    @NonNull
    private String walletAddress;

    @NonNull
    private String salt;

    @NonNull
    private String signature;

    @Positive
    private int signatureVersion;

}
