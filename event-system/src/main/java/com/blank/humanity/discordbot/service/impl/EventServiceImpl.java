package com.blank.humanity.discordbot.service.impl;

import java.util.concurrent.ExecutionException;

import javax.naming.ConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.blank.humanity.discordbot.BlankEvent;
import com.blank.humanity.discordbot.BlankRpcEvent;
import com.blank.humanity.discordbot.config.EventConfiguration;
import com.blank.humanity.discordbot.config.EventServiceConfiguration;
import com.blank.humanity.discordbot.service.EventService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

    @Autowired
    public EventServiceConfiguration eventServiceConfiguration;

    @Autowired
    private SpringEventService springEventService;

    @Autowired
    private KafkaEventService kafkaEventService;

    public void publishEvent(BlankEvent event) throws JsonProcessingException {
        EventConfiguration eventConfig = eventServiceConfiguration
            .getEventConfiguration(event.getEventIdentifier())
            .orElseThrow(() -> new RuntimeException(
                new ConfigurationException("Event Configuration for '"
                    + event.getEventIdentifier()
                    + "' is missing!")));

        if (eventConfig.isPublishLocal()) {
            springEventService.publishEvent(event);
        } else {
            kafkaEventService.publishEvent(event);
        }
    }

    @Async
    public void publishAsyncEvent(BlankEvent event) {
        try {
            publishEvent(event);
        } catch (JsonProcessingException e) {
            log
                .error("An error occured during asynchronous event publishing!",
                    e);
        }
    }

    public <R> R callEvent(BlankRpcEvent<R> respondableEvent)
        throws JsonProcessingException, InterruptedException,
        ExecutionException {
        EventConfiguration eventConfig = eventServiceConfiguration
            .getEventConfiguration(respondableEvent.getEventIdentifier())
            .orElseThrow(() -> new RuntimeException(
                new ConfigurationException("Event Configuration for '"
                    + respondableEvent.getEventIdentifier()
                    + "' is missing!")));

        if (eventConfig.isPublishLocal()) {
            springEventService.publishEvent(respondableEvent);
            return respondableEvent.getResponse();
        } else {
            return kafkaEventService.callEvent(respondableEvent);
        }
    }

    @Async
    public <R> ListenableFuture<R> callAsyncRpcEvent(
        BlankRpcEvent<R> respondableEvent) {
        try {
            return new AsyncResult<>(callEvent(respondableEvent));
        } catch (JsonProcessingException | ExecutionException e) {
            return AsyncResult.forExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AsyncResult.forExecutionException(e);
        }
    }

}
