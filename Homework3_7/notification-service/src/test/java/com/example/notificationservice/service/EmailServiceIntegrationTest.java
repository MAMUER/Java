package com.example.notificationservice.service;

import com.example.notificationservice.TestMailConfig;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EmailServiceIntegrationTest {

    private static final String MAIL_USER = TestMailConfig.MAIL_USER;
    private static final String MAIL_PASSWORD = UUID.randomUUID().toString();
    private static final String SMTP_HOST = ServerSetupTest.SMTP.getBindAddress();
    private static final int SMTP_PORT = ServerSetupTest.SMTP.getPort();

    @RegisterExtension
    static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP)
            .withPerMethodLifecycle(true)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser(MAIL_USER, MAIL_PASSWORD));

    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> SMTP_HOST);
        registry.add("spring.mail.port", () -> SMTP_PORT);
        registry.add("spring.mail.protocol", () -> "smtp");
        registry.add("spring.mail.username", () -> MAIL_USER);
        registry.add("spring.mail.password", () -> MAIL_PASSWORD);
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "true");
        registry.add("spring.mail.properties.mail.smtp.ssl.enable", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        registry.add("app.notification.from", () -> MAIL_USER);
    }

    @Autowired
    private NotificationService notificationService;

    @Test
    void createdNotificationContainsRequiredText() throws Exception {
        notificationService.sendCreated(MAIL_USER);

        MimeMessage message = singleReceivedMessage();
        assertEquals("mihnikolaenko12@yandex.ru", message.getFrom()[0].toString());
        assertEquals(MAIL_USER, message.getAllRecipients()[0].toString());
        assertEquals("Аккаунт создан", message.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                message.getContent().toString());
    }

    @Test
    void deletedNotificationContainsRequiredText() throws Exception {
        notificationService.sendDeleted(MAIL_USER);

        MimeMessage message = singleReceivedMessage();
        assertEquals(MAIL_USER, message.getAllRecipients()[0].toString());
        assertEquals("Аккаунт удалён", message.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", message.getContent().toString());
    }

    private MimeMessage singleReceivedMessage() throws Exception {
        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1));
        MimeMessage[] messages = GREEN_MAIL.getReceivedMessages();
        assertEquals(1, messages.length);
        return messages[0];
    }
}