package de.zorro909.blank.wallet.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import de.zorro909.blank.BlankDiscordBot.entities.user.BlankUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
public class DiscordWalletSalt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NonNull
    @OneToOne
    @JoinColumn(name = "userId", nullable = false, unique = true, referencedColumnName = "id")
    private BlankUser user;

    @NonNull
    private String salt;

}
