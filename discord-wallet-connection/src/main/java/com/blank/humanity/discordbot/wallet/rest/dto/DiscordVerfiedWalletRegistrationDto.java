package com.blank.humanity.discordbot.wallet.rest.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DiscordVerfiedWalletRegistrationDto {

    @NonNull
    private String salt;

    @NonNull
    private String signature;

}
