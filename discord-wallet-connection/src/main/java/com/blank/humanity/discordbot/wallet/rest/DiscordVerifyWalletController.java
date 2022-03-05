package com.blank.humanity.discordbot.wallet.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;
import com.blank.humanity.discordbot.wallet.rest.dto.DiscordVerfiedWalletRegistrationDto;
import com.blank.humanity.discordbot.wallet.service.DiscordWalletService;

import lombok.NonNull;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/wallets/discord")
public class DiscordVerifyWalletController {

    @Autowired
    private DiscordWalletService discordWalletService;

    @PostMapping("registerVerifiedWallet")
    public Mono<ResponseEntity<Void>> registerVerifiedWallet(
        @NonNull DiscordVerfiedWalletRegistrationDto discordWalletRegistrationDto) {
        Optional<DiscordVerifiedWallet> wallet = discordWalletService
            .registerVerifiedWallet(discordWalletRegistrationDto.getSignature(),
                discordWalletRegistrationDto.getSalt());

        if (wallet.isEmpty()) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        return Mono.just(ResponseEntity.ok().build());
    }

}
