package de.zorro909.blank.event.config;

import javax.validation.constraints.NotNull;

import de.zorro909.blank.event.BlankEvent;
import lombok.Data;

@Data
public class EventConfiguration {
    
    @NotNull
    private Class<? extends BlankEvent> eventClass;
    
    private boolean publishLocal = true;
    
    private boolean listenToExternalEvents = false;
    
}
