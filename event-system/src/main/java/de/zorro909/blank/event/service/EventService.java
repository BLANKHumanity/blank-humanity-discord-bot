package de.zorro909.blank.event.service;

import java.util.concurrent.ExecutionException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.zorro909.blank.event.BlankEvent;
import de.zorro909.blank.event.BlankRpcEvent;

public interface EventService {

    public void publishEvent(BlankEvent event) throws JsonProcessingException;

    @Async
    public void publishAsyncEvent(BlankEvent event);

    public <R> R callEvent(BlankRpcEvent<R> respondableEvent)
	    throws JsonProcessingException, InterruptedException,
	    ExecutionException;

    @Async
    public <R> ListenableFuture<R> callAsyncRpcEvent(
	    BlankRpcEvent<R> respondableEvent);

}
