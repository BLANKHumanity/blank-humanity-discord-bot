package de.zorro909.blank.wallet.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import de.zorro909.blank.wallet.entities.DiscordWallet;
import lombok.NonNull;

public interface DiscordWalletDao
	extends JpaRepository<DiscordWallet, Integer> {

    public Optional<DiscordWallet> findByUser(@NonNull BlankUser user);

}
