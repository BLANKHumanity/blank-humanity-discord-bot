package com.blank.humanity.discordbot;

import static org.mockito.Mockito.mock;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.CommandService;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.services.MenuService;
import com.blank.humanity.discordbot.services.MessageService;

import net.dv8tion.jda.api.JDA;

@SpringBootApplication
@SpringBootConfiguration
@EnableAsync(mode = AdviceMode.ASPECTJ)
@EnableScheduling
public class TestSpringBootContext {

    @Bean
    public JDA jda() {
        return mock(JDA.class);
    }
    
    @Bean
    public BlankUserService blankUserService() {
        return mock(BlankUserService.class);
    }
    
    @Bean
    public CommandService commandService() {
        return mock(CommandService.class);
    }
    
    @Bean
    public MenuService menuService() {
        return mock(MenuService.class);
    }
    
    @Bean
    public MessageService messageService() {
        return mock(MessageService.class);
    }
    
    @Bean
    public InventoryService inventoryService() {
        return mock(InventoryService.class);
    }
    
}
