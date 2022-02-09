package com.blank.humanity.discordbot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blank.humanity.discordbot.entities.user.UserClaimData;

@Repository
public interface UserClaimDataDao extends JpaRepository<UserClaimData, Integer> {
}