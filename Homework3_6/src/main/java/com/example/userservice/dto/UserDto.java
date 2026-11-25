package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UserDto(
        Long id,

        @NotBlank(message = "имя не должно быть пустым")
        @Size(min = 2, max = 100, message = "длина имени должна быть от 2 до 100")
        String name,

        @NotBlank(message = "email не должен быть пустым")
        @Email(message = "email не соответствует формату")
        @Size(max = 255, message = "длина email не должна превышать 255")
        String email,

        @NotNull(message = "возраст не должен быть пустым")
        @Min(value = 1, message = "возраст должен быть от 1 до 150")
        @Max(value = 150, message = "возраст должен быть от 1 до 150")
        Integer age,

        LocalDateTime createdAt
) {
}