package com.blank.humanity.discordbot.wallet.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.datatypes.Address;

import com.blank.humanity.discordbot.entities.user.BlankUser;
import com.blank.humanity.discordbot.entities.user.BlankUserMetadata;
import com.blank.humanity.discordbot.services.BlankUserMetadataService;
import com.blank.humanity.discordbot.wallet.entities.AirdropWallet;
import com.blank.humanity.discordbot.wallet.service.AirdropWalletService;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.RestAction;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class AirdropWalletServiceImpl implements AirdropWalletService {

    @Autowired
    private JDA jda;

    @Autowired
    private BlankUserMetadataService metadataService;

    private static final String AIRDROP_METADATA_KEY = "airdropWallet";

    @Transactional
    @Override
    public AirdropWallet setAirdropWallet(BlankUser user, Address wallet) {
        BlankUserMetadata metadata = metadataService
            .saveMetadata(user, AIRDROP_METADATA_KEY, wallet.toString());
        return new AirdropWallet(metadata);
    }

    @Transactional
    @Override
    public Optional<AirdropWallet> getAirdropWallet(BlankUser user) {
        return metadataService
            .getMetadata(user, AIRDROP_METADATA_KEY)
            .map(AirdropWallet::new);
    }

    @Transactional
    @Override
    public Flux<AirdropWallet> listAllAirdropWallets() {
        return metadataService
            .fluxListAllMetadataByKey(AIRDROP_METADATA_KEY)
            .map(AirdropWallet::new);
    }

    @Transactional
    @Override
    public Flux<AirdropWallet> listAirdropWalletsByRole(Long roleId) {
        log.info("Start listing AidropWallets by Role '" + roleId + "'");
        Role role = jda.getRoleById(roleId);

        if (role == null) {
            log.info("Role '" + roleId + "' could not be found!");
            return Flux.empty();
        }

        Guild guild = jda.getGuildById(role.getGuild().getIdLong());

        return listAllAirdropWallets()
            .filter(wallet -> hasRole(wallet, guild, role));
    }

    private boolean hasRole(AirdropWallet wallet, Guild guild, Role role) {
        return Optional
            .ofNullable(guild
                .retrieveMemberById(wallet.getUser().getDiscordId()))
            .map(RestAction::complete)
            .stream()
            .flatMap(member -> member.getRoles().stream())
            .anyMatch(memberRole -> memberRole.getIdLong() == role.getIdLong());
    }

}
