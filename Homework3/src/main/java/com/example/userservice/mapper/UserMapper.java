package com.example.userservice.mapper;

import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class UserMapper {

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final int AGE_MIN = 1;
    private static final int AGE_MAX = 150;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UserMapper() {
    }

    public static User toEntity(UserDto dto) {
        validate(dto);
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
            throw new IllegalArgumentException("entity must not be null");
        }
        return new UserDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getAge(),
                entity.getCreatedAt()
        );
    }

    public static List<String> validateName(String name) {
        List<String> violations = new ArrayList<>();
        if (name == null || name.isBlank()) {
            violations.add("имя не должно быть пустым");
            return violations;
        }
        String trimmed = name.trim();
        if (trimmed.length() < NAME_MIN_LENGTH || trimmed.length() > NAME_MAX_LENGTH) {
            violations.add("длина имени должна быть от " + NAME_MIN_LENGTH
                    + " до " + NAME_MAX_LENGTH);
        }
        return violations;
    }

    public static List<String> validateEmail(String email) {
        List<String> violations = new ArrayList<>();
        if (email == null || email.isBlank()) {
            violations.add("email не должен быть пустым");
            return violations;
        }
        String trimmed = email.trim();
        if (trimmed.length() > EMAIL_MAX_LENGTH) {
            violations.add("длина email не должна превышать " + EMAIL_MAX_LENGTH);
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            violations.add("email не соответствует формату");
        }
        return violations;
    }

    public static List<String> validateAge(Integer age) {
        List<String> violations = new ArrayList<>();
        if (age == null) {
            violations.add("возраст не должен быть пустым");
        } else if (age < AGE_MIN || age > AGE_MAX) {
            violations.add("возраст должен быть от " + AGE_MIN + " до " + AGE_MAX);
        }
        return violations;
    }

    public static void validate(UserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }
        List<String> violations = new ArrayList<>();
        violations.addAll(validateName(dto.name()));
        violations.addAll(validateEmail(dto.email()));
        violations.addAll(validateAge(dto.age()));

        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }
}
