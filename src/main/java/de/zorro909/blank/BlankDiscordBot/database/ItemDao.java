package de.zorro909.blank.BlankDiscordBot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import de.zorro909.blank.BlankDiscordBot.entities.Item;

@Repository
public interface ItemDao extends JpaRepository<Item, Integer> {

    @Query("SELECT coalesce(sum(i.amount),0) FROM Item i WHERE i.itemId = ?1")
    public int sumOfAllExistingItems(int itemId);

}
