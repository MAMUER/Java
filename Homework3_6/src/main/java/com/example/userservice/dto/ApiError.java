package com.example.userservice.dto;

import java.util.List;

public record ApiError(String message, List<String> details) {

    public static ApiError of(String message) {
        return new ApiError(message, List.of());
    }
}
