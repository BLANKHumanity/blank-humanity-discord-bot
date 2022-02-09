package com.blank.humanity.discordbot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.item.Item;

@Repository
public interface ItemDao extends JpaRepository<Item, Integer> {

}
