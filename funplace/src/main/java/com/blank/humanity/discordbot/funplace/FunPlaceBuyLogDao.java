package com.blank.humanity.discordbot.funplace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.BlankUser;

@Repository
public interface FunPlaceBuyLogDao extends JpaRepository<FunPlaceBuyLogEntry, Integer> {

    public List<FunPlaceBuyLogEntry> findByBuyer(BlankUser buyer);
    
    @Query("SELECT COALESCE(SUM(b.amount),0) FROM FunPlaceBuyLogEntry b WHERE b.shopId = ?1")
    public int sumOfBoughtItems(int shopId);
    
}
