package com.example.notificationservice.listener;

import com.example.events.UserEvent;
import com.example.events.UserOperation;
import com.example.notificationservice.TestMailConfig;
import com.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {

    private static final String MAIL = TestMailConfig.MAIL_USER;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserEventListener listener;

    @Test
    void createdEventSendsCreatedNotification() {
        UserEvent event = new UserEvent("1", UserOperation.CREATED, MAIL, "2026-01-01T10:00:00");

        listener.onUserEvent(event);

        verify(notificationService).sendFor(UserOperation.CREATED, MAIL);
    }

    @Test
    void deletedEventSendsDeletedNotification() {
        UserEvent event = new UserEvent("2", UserOperation.DELETED, MAIL, "2026-01-01T10:00:00");

        listener.onUserEvent(event);

        verify(notificationService).sendFor(UserOperation.DELETED, MAIL);
    }

    @Test
    void failedNotificationDoesNotBreakListener() {
        UserEvent event = new UserEvent("3", UserOperation.CREATED, MAIL, "2026-01-01T10:00:00");
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                .when(notificationService).sendFor(UserOperation.CREATED, MAIL);

        listener.onUserEvent(event);

        verify(notificationService).sendFor(UserOperation.CREATED, MAIL);
    }
}