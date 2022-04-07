package de.zorro909.blank.event.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zorro909.blank.event.BlankEvent;
import de.zorro909.blank.event.BlankRpcEvent;
import de.zorro909.blank.event.config.EventConfiguration;
import de.zorro909.blank.event.config.EventServiceConfiguration;

@Service
class KafkaEventService implements BatchMessageListener<String, String> {

    @Autowired
    private EventServiceConfiguration eventServiceConfiguration;

    @Autowired
    private SpringEventService eventService;

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    KafkaListenerContainerFactory<GenericMessageListenerContainer<String, String>> containerFactory;

    private HashMap<String, GenericMessageListenerContainer<String, String>> eventListeners;

    @PostConstruct
    private void registerEvents() {
	KafkaEventService kafkaEventService = this;

	eventServiceConfiguration
		.getEventConfiguration()
		.entrySet()
		.stream()
		.filter(entry -> entry.getValue().isListenToExternalEvents())
		.forEach(entry -> {
		    GenericMessageListenerContainer<String, String> container = containerFactory
			    .createContainer(entry.getKey());
		    container.setAutoStartup(false);
		    container.setupMessageListener(kafkaEventService);
		    eventListeners.put(entry.getKey(), container);
		    container.start();
		});
    }

    public ListenableFuture<SendResult<String, String>> publishEvent(
	    BlankEvent event) throws JsonProcessingException {
	return replyingKafkaTemplate
		.send(event.getEventIdentifier(),
			mapper.writeValueAsString(event));
    }

    public <R> R callEvent(BlankRpcEvent<R> rpcEvent)
	    throws InterruptedException, ExecutionException,
	    JsonProcessingException {
	String responsePayload = replyingKafkaTemplate
		.sendAndReceive(
			new ProducerRecord<>(rpcEvent.getEventIdentifier(),
				mapper.writeValueAsString(rpcEvent)))
		.get()
		.value();
	return mapper.readValue(responsePayload, rpcEvent.getResponseType());
    }

    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data) {
	for (ConsumerRecord<String, String> consumerRecord : data) {
	    try {
		Class<? extends BlankEvent> eventClass = eventServiceConfiguration
			.getEventConfiguration(consumerRecord.topic())
			.map(EventConfiguration::getEventClass)
			.orElseThrow(() -> new ClassNotFoundException(
				"No Class for Event Identifier '"
					+ consumerRecord.topic()
					+ "' could be found!"));

		BlankEvent event = mapper
			.readValue(consumerRecord.value(), eventClass);

		eventService.publishEvent(event);
		if (event instanceof BlankRpcEvent) {
		    Object response = ((BlankRpcEvent<?>) event).getResponse();
		    if (response != null) {
			sendEventResponse(consumerRecord, response);
		    }
		}
	    } catch (ClassNotFoundException notFound) {
	    } catch (JsonProcessingException processingException) {

	    }
	}
    }

    private void sendEventResponse(
	    ConsumerRecord<String, String> consumerRecord, Object response)
	    throws JsonProcessingException {
	Headers headers = consumerRecord.headers();
	String replyTopic = new String(
		headers.lastHeader(KafkaHeaders.REPLY_TOPIC).value());
	Header correlationId = headers.lastHeader(KafkaHeaders.CORRELATION_ID);

	String responseJson = mapper.writeValueAsString(response);

	ProducerRecord<String, String> responseDto = new ProducerRecord<>(
		replyTopic, responseJson);
	responseDto.headers().add(correlationId);
	replyingKafkaTemplate.send(responseDto);
    }

}
