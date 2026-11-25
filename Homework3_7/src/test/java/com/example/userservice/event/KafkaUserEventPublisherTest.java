package com.example.userservice.event;

import com.example.events.UserEvent;
import com.example.events.UserOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaUserEventPublisherTest {

    @Mock
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    private KafkaUserEventPublisher publisher() {
        return new KafkaUserEventPublisher(kafkaTemplate, "user-events");
    }

    @Test
    void createdEventIsPublishedWithEmailAsKey() {
        when(kafkaTemplate.send(eq("user-events"), eq("ivan@example.com"), any(UserEvent.class)))
                .thenReturn(new CompletableFuture<>());

        publisher().publish(UserOperation.CREATED, "ivan@example.com");

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
        verify(kafkaTemplate).send(eq("user-events"), eq("ivan@example.com"), captor.capture());
        UserEvent event = captor.getValue();
        assertEquals(UserOperation.CREATED, event.operation());
        assertEquals("ivan@example.com", event.email());
        assertNotNull(event.eventId());
        assertNotNull(event.occurredAt());
    }

    @Test
    void deletedEventIsPublishedWithEmailAsKey() {
        when(kafkaTemplate.send(eq("user-events"), eq("ivan@example.com"), any(UserEvent.class)))
                .thenReturn(new CompletableFuture<>());

        publisher().publish(UserOperation.DELETED, "ivan@example.com");

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
        verify(kafkaTemplate).send(eq("user-events"), eq("ivan@example.com"), captor.capture());
        assertEquals(UserOperation.DELETED, captor.getValue().operation());
    }

    @Test
    void brokerFailureDoesNotStopUserOperation() {
        when(kafkaTemplate.send(eq("user-events"), eq("ivan@example.com"), any(UserEvent.class)))
                .thenThrow(new IllegalStateException("broker down"));

        assertDoesNotThrow(() -> publisher().publish(UserOperation.CREATED, "ivan@example.com"));
    }
}