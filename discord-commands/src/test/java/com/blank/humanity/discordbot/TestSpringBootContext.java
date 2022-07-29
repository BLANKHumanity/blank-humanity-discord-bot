package com.blank.humanity.discordbot;

import java.security.SecureRandom;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;

import com.blank.humanity.discordbot.service.EventService;
import com.blank.humanity.discordbot.services.BlankUserMetadataService;
import com.blank.humanity.discordbot.services.BlankUserService;
import com.blank.humanity.discordbot.services.CommandService;
import com.blank.humanity.discordbot.services.InventoryService;
import com.blank.humanity.discordbot.services.MenuService;
import com.blank.humanity.discordbot.services.MessageService;

import net.dv8tion.jda.api.JDA;

@EnableAspectJAutoProxy(proxyTargetClass = false)
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
    
    @MockBean
    public BlankUserMetadataService blankUserMetadataService;
    
    @MockBean
    public EnsResolver ensResolver;
    
    @MockBean
    public Web3j web3j;
    
    @MockBean
    public EventService eventService;
    
    @MockBean
    public RestTemplate restTemplate;
    
    @Bean
    public SecureRandom random() {
        return new SecureRandom();
    }
}
