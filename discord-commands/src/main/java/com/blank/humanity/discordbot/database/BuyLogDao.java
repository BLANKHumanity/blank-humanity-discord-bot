package com.blank.humanity.discordbot.database;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.item.BuyLogEntry;
import com.blank.humanity.discordbot.entities.user.BlankUser;

@Repository
public interface BuyLogDao extends JpaRepository<BuyLogEntry, Integer> {

    public List<BuyLogEntry> findByBuyer(BlankUser buyer);
    
    @Query("SELECT COALESCE(SUM(b.amount),0) FROM BuyLogEntry b WHERE b.shopId = ?1")
    public int sumOfBoughtItems(int shopId);
    
}
