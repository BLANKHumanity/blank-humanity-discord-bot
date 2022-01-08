package de.zorro909.blank.BlankDiscordBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync(mode = AdviceMode.ASPECTJ)
@EnableCaching
@EnableScheduling
public class BlankDiscordBotApplication {

    public static void main(String[] args) {
	SpringApplication.run(BlankDiscordBotApplication.class, args);
    }

}
