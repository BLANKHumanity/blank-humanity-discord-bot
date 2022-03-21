package com.blank.humanity.discordbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableAsync(mode = AdviceMode.ASPECTJ)
@EnableCaching
@EnableScheduling
@EnableWebFlux
public class BlankDiscordBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlankDiscordBotApplication.class, args);
    }

}
