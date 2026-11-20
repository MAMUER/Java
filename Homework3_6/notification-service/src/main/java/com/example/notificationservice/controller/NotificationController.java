package com.example.notificationservice.controller;

import com.example.events.UserOperation;
import com.example.notificationservice.dto.EmailRequest;
import com.example.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@Valid @RequestBody EmailRequest request) {
        UserOperation operation = parseOperation(request.operation());
        notificationService.sendFor(operation, request.email());
    }

    private UserOperation parseOperation(String raw) {
        try {
            return UserOperation.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестная операция: " + raw, e);
        }
    }
}