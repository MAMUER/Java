package com.example.notificationservice.service;

import com.example.events.UserOperation;
import com.example.notificationservice.exception.NotificationException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final String CREATED_TEMPLATE = "Здравствуйте! Ваш аккаунт на сайте %s был успешно создан.";
    private static final String DELETED_TEXT = "Здравствуйте! Ваш аккаунт был удалён.";
    private static final String CREATED_SUBJECT = "Аккаунт создан";
    private static final String DELETED_SUBJECT = "Аккаунт удалён";

    private final EmailService emailService;
    private final String siteName;

    public NotificationService(EmailService emailService,
                               @Value("${app.notification.site-name:ваш сайт}") String siteName) {
        this.emailService = emailService;
        this.siteName = siteName;
    }

    public void sendFor(UserOperation operation, String email) {
        if (operation == null) {
            throw new IllegalArgumentException("operation не должна быть null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email не должен быть пустым");
        }
        switch (operation) {
            case CREATED -> sendCreated(email);
            case DELETED -> sendDeleted(email);
        }
    }

    public void sendCreated(String email) {
        send(email, CREATED_SUBJECT, String.format(CREATED_TEMPLATE, siteName));
    }

    public void sendDeleted(String email) {
        send(email, DELETED_SUBJECT, DELETED_TEXT);
    }

    private void send(String email, String subject, String body) {
        try {
            emailService.send(email, subject, body);
        } catch (MessagingException e) {
            throw new NotificationException("Не удалось отправить письмо для " + email, e);
        }
    }
}