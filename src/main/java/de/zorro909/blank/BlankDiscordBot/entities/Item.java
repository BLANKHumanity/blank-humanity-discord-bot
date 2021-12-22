package de.zorro909.blank.BlankDiscordBot.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items")
@Data
@Validated
@NoArgsConstructor
public class Item {

    public Item(int itemId, int amount, BlankUser user) {
	this.itemId = itemId;
	this.amount = amount;
	this.owner = user;
    }

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @Min(0)
    private int itemId;

    @NotNull
    @Min(1)
    private int amount;

    @NotNull
    @Lazy
    @ManyToOne(optional = false)
    private BlankUser owner;

}
