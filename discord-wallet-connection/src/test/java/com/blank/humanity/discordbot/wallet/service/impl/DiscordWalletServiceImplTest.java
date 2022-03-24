package com.blank.humanity.discordbot.wallet.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.utils.Numeric;

@ExtendWith(MockitoExtension.class)
class DiscordWalletServiceImplTest {

    @InjectMocks
    private DiscordWalletServiceImpl discordWalletService;

    @Test
    void testRecoverAddressFromSignature() {
        String testMessage = "Testing Message";
        String signature = "0x14b4dbb0c6d3f9da279da1fc273de949a9528e4e9f357bfc9da6590a19a609e93cf3d0be1d53e89b295dcae4efa9fe35e1f4804e269829bf2ae61a0f7c8e05761b";

        Optional<String> address = discordWalletService
            .recoverAddressFromSignature(testMessage.getBytes(),
                Numeric.hexStringToByteArray(signature));

        assertThat(address)
            .isPresent()
            .hasValueSatisfying(value -> value
                .equalsIgnoreCase(
                    "0x505fAe1560E6dc7c29fef525008FF6bE91dd6548"));
    }

}
