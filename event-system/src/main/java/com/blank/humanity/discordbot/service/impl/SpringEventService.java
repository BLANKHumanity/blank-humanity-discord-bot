package com.blank.humanity.discordbot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.blank.humanity.discordbot.BlankEvent;

@Service
class SpringEventService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publishEvent(BlankEvent event) {
	eventPublisher.publishEvent(event);
    }

}
