package com.example.notificationservice.controller;

import com.example.notificationservice.TestMailConfig;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerIntegrationTest {

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
    private MockMvc mockMvc;

    @Test
    void createdOperationSendsEmail() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mihnikolaenko12@yandex.ru","operation":"CREATED"}
                                """))
                .andExpect(status().isAccepted());

        MimeMessage message = singleReceivedMessage();
        assertEquals(MAIL_USER, message.getAllRecipients()[0].toString());
        assertEquals("Аккаунт создан", message.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                message.getContent().toString());
    }

    @Test
    void deletedOperationSendsEmail() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mihnikolaenko12@yandex.ru","operation":"DELETED"}
                                """))
                .andExpect(status().isAccepted());

        MimeMessage message = singleReceivedMessage();
        assertEquals(MAIL_USER, message.getAllRecipients()[0].toString());
        assertEquals("Аккаунт удалён", message.getSubject());
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", message.getContent().toString());
    }

    @Test
    void lowercaseOperationIsAccepted() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mihnikolaenko12@yandex.ru","operation":"deleted"}
                                """))
                .andExpect(status().isAccepted());

        assertEquals(1, GREEN_MAIL.getReceivedMessages().length);
    }

    @Test
    void invalidEmailIsRejectedWithoutSending() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bad-email","operation":"CREATED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, GREEN_MAIL.getReceivedMessages().length);
    }

    @Test
    void unknownOperationIsRejectedWithoutSending() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mihnikolaenko12@yandex.ru","operation":"REMOVED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неизвестная операция: REMOVED"));

        assertEquals(0, GREEN_MAIL.getReceivedMessages().length);
    }

    private MimeMessage singleReceivedMessage() throws Exception {
        assertTrue(GREEN_MAIL.waitForIncomingEmail(5000, 1));
        MimeMessage[] messages = GREEN_MAIL.getReceivedMessages();
        assertEquals(1, messages.length);
        return messages[0];
    }
}