package de.zorro909.blank.BlankDiscordBot.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.BlankDiscordBot.wallet.entities.DiscordWallet;
import lombok.NonNull;

@Repository
public interface DiscordWalletDao
	extends JpaRepository<DiscordWallet, Integer> {

    public Optional<DiscordWallet> findByUser(@NonNull BlankUser user);

}
