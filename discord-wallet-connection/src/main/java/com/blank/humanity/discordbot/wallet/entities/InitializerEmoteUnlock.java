package com.blank.humanity.discordbot.wallet.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "initializer",
    "emote" }))
public class InitializerEmoteUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int initializer;

    private String emote;

}
