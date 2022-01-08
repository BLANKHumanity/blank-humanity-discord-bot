package de.zorro909.blank.BlankDiscordBot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.zorro909.blank.BlankDiscordBot.entities.item.Item;

@Repository
public interface ItemDao extends JpaRepository<Item, Integer> {

}
