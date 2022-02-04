package de.zorro909.blank.wallet.rest;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DiscordWalletRegistrationDto {

    @NonNull
    private String salt;

    @NonNull
    private String signature;

}
