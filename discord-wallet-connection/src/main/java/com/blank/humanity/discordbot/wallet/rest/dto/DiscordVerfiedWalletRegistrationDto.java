package com.blank.humanity.discordbot.wallet.rest.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DiscordVerfiedWalletRegistrationDto {

    private String address;

    private String salt;

    private String signature;

}
