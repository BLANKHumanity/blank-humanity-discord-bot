package com.blank.humanity.discordbot.wallet.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blank.humanity.discordbot.wallet.entities.DiscordVerifiedWallet;
import com.blank.humanity.discordbot.wallet.rest.dto.DiscordVerfiedWalletRegistrationDto;
import com.blank.humanity.discordbot.wallet.service.DiscordWalletService;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/wallets/discord")
public class DiscordVerifyWalletController {

    @Autowired
    private DiscordWalletService discordWalletService;

    @PostMapping(value = "registerVerifiedWallet", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> registerVerifiedWallet(
        @RequestBody @NonNull DiscordVerfiedWalletRegistrationDto discordWalletRegistrationDto) {
        log
            .info("Discod Wallet Registration Request:"
                + discordWalletRegistrationDto.toString());
        Optional<DiscordVerifiedWallet> wallet = discordWalletService
            .registerVerifiedWallet(discordWalletRegistrationDto.getSignature(),
                discordWalletRegistrationDto.getSalt());

        if (wallet.isEmpty()) {
            return Mono.just(ResponseEntity.notFound().build());
        }
        return Mono.just(ResponseEntity.ok().build());
    }

}
