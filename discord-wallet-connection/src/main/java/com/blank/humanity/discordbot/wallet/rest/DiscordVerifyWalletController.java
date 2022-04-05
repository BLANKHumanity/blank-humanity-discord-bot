package com.blank.humanity.discordbot.wallet.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        ResponseEntity<Void> statusEntity = discordWalletService
            .registerVerifiedWallet(discordWalletRegistrationDto.getAddress(),
                discordWalletRegistrationDto.getSignature(),
                discordWalletRegistrationDto.getSalt());

        return Mono.just(statusEntity);
    }

}
