package de.zorro909.blank.BlankDiscordBot.entities.item;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyLogEntry {

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