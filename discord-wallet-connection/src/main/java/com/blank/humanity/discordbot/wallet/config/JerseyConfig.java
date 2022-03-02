package com.blank.humanity.discordbot.wallet.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import com.blank.humanity.discordbot.wallet.rest.DiscordVerifyWalletController;

@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
	register(DiscordVerifyWalletController.class);
    }

}
