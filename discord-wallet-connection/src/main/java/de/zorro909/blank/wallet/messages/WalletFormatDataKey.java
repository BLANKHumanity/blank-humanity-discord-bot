package de.zorro909.blank.wallet.messages;

import de.zorro909.blank.BlankDiscordBot.utils.FormatDataKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum WalletFormatDataKey implements FormatDataKey {
    WALLET_VERIFY_LINK("walletVerifyLink");

    @NonNull
    private String key;

    private boolean required = false;

}
