package com.example.userservice.mapper;

import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperTest {

    @Test
    void validDtoPasses() {
        UserDto dto = new UserDto(null, "Иван", "ivan@example.com", 30, LocalDateTime.now());
        UserMapper.validate(dto);
    }

    @Test
    void invalidAgeThrows() {
        UserDto dto = new UserDto(null, "Иван", "ivan@example.com", 200, null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> UserMapper.validate(dto));
        assertTrue(ex.getViolations().stream().anyMatch(v -> v.contains("возраст")));
    }

    @Test
    void invalidNameThrows() {
        UserDto dto = new UserDto(null, "A", "ivan@example.com", 30, null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> UserMapper.validate(dto));
        assertTrue(ex.getViolations().stream().anyMatch(v -> v.contains("имен")));
    }

    @Test
    void invalidEmailThrows() {
        UserDto dto = new UserDto(null, "Иван", "not-an-email", 30, null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> UserMapper.validate(dto));
        assertTrue(ex.getViolations().stream().anyMatch(v -> v.contains("email")));
    }
}
