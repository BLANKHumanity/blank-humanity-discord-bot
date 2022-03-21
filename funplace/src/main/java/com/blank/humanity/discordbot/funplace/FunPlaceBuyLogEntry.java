package com.blank.humanity.discordbot.funplace;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.blank.humanity.discordbot.entities.user.BlankUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunPlaceBuyLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    private int shopId;
    
    @NotNull
    private int amount;

    @NotNull
    @ManyToOne(optional = false)
    private BlankUser buyer;
}
