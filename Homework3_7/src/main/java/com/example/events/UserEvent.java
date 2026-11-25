package com.example.events;

public record UserEvent(
        String eventId,
        UserOperation operation,
        String email,
        String occurredAt
) {
}