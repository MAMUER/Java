package com.example.userservice.event;

import com.example.events.UserEvent;
import com.example.events.UserOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class KafkaUserEventPublisher implements UserEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaUserEventPublisher.class);

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;

    public KafkaUserEventPublisher(KafkaTemplate<String, UserEvent> kafkaTemplate,
                                   @Value("${app.kafka.topic:user-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(UserOperation operation, String email) {
        UserEvent event = new UserEvent(
                UUID.randomUUID().toString(),
                operation,
                email,
                LocalDateTime.now(ZoneId.systemDefault()).toString());
        try {
            kafkaTemplate.send(topic, email, event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            LOGGER.error("Не удалось отправить событие {} для {}", operation, email, error);
                        } else {
                            LOGGER.info("Отправлено событие {} для {} в топик {}",
                                    operation, email, topic);
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Не удалось отправить событие {} для {}", operation, email, e);
        }
    }
}