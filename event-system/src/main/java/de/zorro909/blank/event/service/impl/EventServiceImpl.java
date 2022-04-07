package de.zorro909.blank.event.service.impl;

import java.util.concurrent.ExecutionException;
import javax.naming.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.zorro909.blank.event.BlankEvent;
import de.zorro909.blank.event.BlankRpcEvent;
import de.zorro909.blank.event.config.EventConfiguration;
import de.zorro909.blank.event.config.EventServiceConfiguration;
import de.zorro909.blank.event.service.EventService;

@Service
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
	    // TODO Auto-generated catch block
	    e.printStackTrace();
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
