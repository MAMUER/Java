package com.example.userservice.mapper;

import com.example.userservice.dto.UserDto;
import com.example.userservice.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto не должен быть null");
        }
        User entity = new User();
        entity.setId(dto.id());
        entity.setName(dto.name().trim());
        entity.setEmail(dto.email().trim());
        entity.setAge(dto.age());
        entity.setCreatedAt(dto.createdAt());
        return entity;
    }

    public static UserDto toDto(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity не должен быть null");
        }
        return new UserDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getAge(),
                entity.getCreatedAt()
        );
    }
}