package com.example.notificationservice.listener;

import com.example.events.UserEvent;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventListener.class);

    private final NotificationService notificationService;

    public UserEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topic:user-events}")
    public void onUserEvent(UserEvent event) {
        LOGGER.info("Получено событие {} для {}", event.operation(), event.email());
        try {
            notificationService.sendFor(event.operation(), event.email());
        } catch (RuntimeException e) {
            LOGGER.error("Не удалось обработать событие {} для {}",
                    event.operation(), event.email(), e);
        }
    }
}