package com.blank.humanity.discordbot.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
@ConfigurationProperties
@PropertySource("classpath:config/events.properties")
@Configuration
public class EventServiceConfiguration {

    private Map<String, EventConfiguration> eventConfiguration;

    private String replyTopic;

    private String bootstrapServer;

    private String groupId;

    public Optional<EventConfiguration> getEventConfiguration(
        String eventIdentifier) {
        return Optional.ofNullable(eventConfiguration.get(eventIdentifier));
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        return new SimpleApplicationEventMulticaster();
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props
            .put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props
            .put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public GenericMessageListenerContainer<String, String> messageListenerContainer(
        ConcurrentKafkaListenerContainerFactory<String, String> containerFactory,
        ConsumerFactory<String, String> consumerFactory) {
        containerFactory.setConsumerFactory(consumerFactory);
        return containerFactory.createContainer(getReplyTopic());
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps
            .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProps
            .put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps
            .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String> kafkaTemplate(
        ProducerFactory<String, String> producerFactory,
        GenericMessageListenerContainer<String, String> listenerContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory(),
            listenerContainer);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
