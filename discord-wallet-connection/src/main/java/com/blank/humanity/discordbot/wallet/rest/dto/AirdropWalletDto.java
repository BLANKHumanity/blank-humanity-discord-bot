package com.blank.humanity.discordbot.wallet.rest.dto;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record AirdropWalletDto(@NonNull String user, @NonNull Long discordId,
    @Nullable String wallet) {
}
