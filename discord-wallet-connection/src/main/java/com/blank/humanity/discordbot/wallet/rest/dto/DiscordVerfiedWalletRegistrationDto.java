package com.blank.humanity.discordbot.wallet.rest.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DiscordVerfiedWalletRegistrationDto {

    @NonNull
    private String salt;

    @NonNull
    private String signature;

}
