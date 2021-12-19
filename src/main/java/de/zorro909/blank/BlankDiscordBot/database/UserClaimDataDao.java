package de.zorro909.blank.BlankDiscordBot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.zorro909.blank.BlankDiscordBot.entities.UserClaimData;

@Repository
public interface UserClaimDataDao extends JpaRepository<UserClaimData, Integer> {
}