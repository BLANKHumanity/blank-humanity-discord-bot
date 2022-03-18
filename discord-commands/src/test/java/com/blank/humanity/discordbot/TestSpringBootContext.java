package com.blank.humanity.discordbot;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.AdviceMode;
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

    @MockBean
    public JDA jda;
    
    @MockBean
    public BlankUserService blankUserService;
    
    @MockBean
    public CommandService commandService;
    
    @MockBean
    public MenuService menuService;
    
    @MockBean
    public MessageService messageService;
    
    @MockBean
    public InventoryService inventoryService;
    
}
