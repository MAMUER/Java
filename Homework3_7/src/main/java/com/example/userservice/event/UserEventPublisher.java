package com.example.userservice.event;

import com.example.events.UserOperation;

public interface UserEventPublisher {

    void publish(UserOperation operation, String email);
}