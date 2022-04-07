package de.zorro909.blank.event.config;

import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import de.zorro909.blank.event.BlankEvent;
import lombok.Data;

@Data
@ConfigurationProperties
@PropertySource("events.properties")
@Configuration
public class EventServiceConfiguration {

    private Map<String, EventConfiguration> eventConfiguration;

    private String replyTopic;

    public Optional<EventConfiguration> getEventConfiguration(
	    String eventIdentifier) {
	return Optional.ofNullable(eventConfiguration.get(eventIdentifier));
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
	return new SimpleApplicationEventMulticaster();
    }

    @Bean
    public GenericMessageListenerContainer<String, BlankEvent> messageListenerContainer(
	    KafkaListenerContainerFactory<GenericMessageListenerContainer<String, BlankEvent>> containerFactory) {
	return containerFactory.createContainer(getReplyTopic());
    }

}
