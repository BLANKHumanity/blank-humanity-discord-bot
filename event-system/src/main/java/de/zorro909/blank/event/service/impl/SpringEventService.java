package de.zorro909.blank.event.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import de.zorro909.blank.event.BlankEvent;

@Service
class SpringEventService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishEvent(BlankEvent event) {
	eventPublisher.publishEvent(event);
    }

}
