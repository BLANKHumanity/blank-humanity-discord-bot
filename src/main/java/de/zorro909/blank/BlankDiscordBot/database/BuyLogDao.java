package de.zorro909.blank.BlankDiscordBot.database;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import de.zorro909.blank.BlankDiscordBot.entities.BlankUser;
import de.zorro909.blank.BlankDiscordBot.entities.BuyLogEntry;

@Repository
public interface BuyLogDao extends JpaRepository<BuyLogEntry, Integer> {

    public List<BuyLogEntry> findByBuyer(BlankUser buyer);
    
    @Query("SELECT count(b) FROM BuyLogEntry b WHERE b.shopId = ?1")
    public int sumOfBoughtItems(int shopId);
    
}
