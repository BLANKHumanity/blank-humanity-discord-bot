package de.zorro909.blank.BlankDiscordBot.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.wallet.entities.DiscordWalletSalt;

@Repository
public interface DiscordWalletSaltDao extends JpaRepository<DiscordWalletSalt, Integer>{

    public Optional<DiscordWalletSalt> findByUser(BlankUser user);
    
    public Optional<DiscordWalletSalt> findBySalt(String salt);
    
}
