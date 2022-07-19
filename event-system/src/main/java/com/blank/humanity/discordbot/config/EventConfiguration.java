package com.blank.humanity.discordbot.config;

import javax.validation.constraints.NotNull;

import com.blank.humanity.discordbot.BlankEvent;

import lombok.Data;

@Data
public class EventConfiguration {
    
    @NotNull
    private Class<? extends BlankEvent> eventClass;
    
    private boolean publishLocal = true;
    
    private boolean listenToExternalEvents = false;
    
}
