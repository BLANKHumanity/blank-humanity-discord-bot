package com.blank.humanity.discordbot.wallet.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.wallet.rest.dto.AirdropWalletDto;
import com.blank.humanity.discordbot.wallet.service.AirdropWalletService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/wallets/airdrop")
public class AirdropWalletListController {

    @Autowired
    private BlankUserService blankUserService;

    @Autowired
    private AirdropWalletService airdropWalletService;

    @GetMapping(path = "role/{role}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AirdropWalletDto> fetchWalletsByRole(
        @PathVariable("role") long role) {
        return airdropWalletService
            .listAirdropWalletsByRole(role)
            .map(wallet -> wallet.toDto(blankUserService));
    }

    @GetMapping(path = "all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AirdropWalletDto> fetchAllWallets() {
        return airdropWalletService
            .listAllAirdropWallets()
            .map(wallet -> wallet.toDto(blankUserService));
    }

}
