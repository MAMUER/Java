package com.example.userservice.mapper;

import com.example.userservice.dto.UserDto;
import com.example.userservice.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserMapperTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Test
    void toEntityMapsFieldsAndTrimsValues() {
        User entity = UserMapper.toEntity(
                new UserDto(5L, "  Ivan  ", " ivan@example.com ", 30, CREATED_AT));

        assertEquals(5L, entity.getId());
        assertEquals("Ivan", entity.getName());
        assertEquals("ivan@example.com", entity.getEmail());
        assertEquals(30, entity.getAge());
        assertEquals(CREATED_AT, entity.getCreatedAt());
    }

    @Test
    void toDtoMapsFields() {
        User entity = new User();
        entity.setId(7L);
        entity.setName("Anna");
        entity.setEmail("anna@example.com");
        entity.setAge(25);
        entity.setCreatedAt(CREATED_AT);

        UserDto dto = UserMapper.toDto(entity);

        assertEquals(7L, dto.id());
        assertEquals("Anna", dto.name());
        assertEquals("anna@example.com", dto.email());
        assertEquals(25, dto.age());
        assertEquals(CREATED_AT, dto.createdAt());
    }

    @Test
    void toEntityRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> UserMapper.toEntity(null));
    }

    @Test
    void toDtoRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> UserMapper.toDto(null));
    }
}