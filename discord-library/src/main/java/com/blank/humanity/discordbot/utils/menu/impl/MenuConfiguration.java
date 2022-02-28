package com.blank.humanity.discordbot.utils.menu.impl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MenuConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ComponentMenu componentMenu() {
        return new ComponentMenu(Duration.of(1, ChronoUnit.MINUTES));
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ReactionMenu reactionMenu() {
        return new ReactionMenu(Duration.of(1, ChronoUnit.MINUTES));
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ComponentMenuBuilder componentMenuBuilder(
        ComponentMenu componentMenu) {
        return new ComponentMenuBuilder(componentMenu);
    }

}
