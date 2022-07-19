package com.blank.humanity.discordbot.wallet.messages;

import com.blank.humanity.discordbot.utils.FormatDataKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum WalletFormatDataKey implements FormatDataKey {
    WALLET_VERIFY_LINK("walletVerifyLink"),
    AIRDROP_WALLET("airdropWallet");

    @NonNull
    private String key;

    private boolean required = false;

}
