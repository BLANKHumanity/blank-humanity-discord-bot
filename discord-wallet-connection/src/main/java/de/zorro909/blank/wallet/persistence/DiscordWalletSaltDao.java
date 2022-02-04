package de.zorro909.blank.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.wallet.entities.DiscordWalletSalt;

public interface DiscordWalletSaltDao extends JpaRepository<DiscordWalletSalt, Integer>{

    public Optional<DiscordWalletSalt> findByUser(BlankUser user);
    
    public Optional<DiscordWalletSalt> findBySalt(String salt);
    
}
