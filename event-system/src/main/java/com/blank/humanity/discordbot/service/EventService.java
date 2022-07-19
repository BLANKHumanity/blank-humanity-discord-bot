package com.blank.humanity.discordbot.service;

import java.util.concurrent.ExecutionException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;

import com.blank.humanity.discordbot.BlankEvent;
import com.blank.humanity.discordbot.BlankRpcEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

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
