package com.example.notificationservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String message,
        List<String> details
) {
}