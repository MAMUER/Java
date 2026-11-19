package com.example.userservice.dto;

public record UserRequest(
        String name,
        String email,
        Integer age
) {
}
