package de.zorro909.blank.BlankDiscordBot.wallet.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import de.zorro909.blank.BlankDiscordBot.wallet.rest.DiscordWalletController;

@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
	register(DiscordWalletController.class);
    }

}
