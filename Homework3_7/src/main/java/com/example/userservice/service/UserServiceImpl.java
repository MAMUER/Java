package com.example.userservice.service;

import com.example.events.UserOperation;
import com.example.userservice.dto.UserDto;
import com.example.userservice.event.UserEventPublisher;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String EMAIL_TAKEN = "пользователь с email %s уже существует";

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    public UserServiceImpl(UserRepository userRepository, UserEventPublisher userEventPublisher) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository не должен быть null");
        }
        if (userEventPublisher == null) {
            throw new IllegalArgumentException("userEventPublisher не должен быть null");
        }
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public UserDto create(UserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto не должен быть null");
        }
        UserDto normalized = normalize(dto);
        validate(normalized);
        String email = normalized.email();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException(List.of(EMAIL_TAKEN.formatted(email)));
        }
        User entity = UserMapper.toEntity(
                new UserDto(null, normalized.name(), email, normalized.age(), null));
        UserDto created;
        try {
            created = UserMapper.toDto(userRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(List.of(EMAIL_TAKEN.formatted(email)), e);
        }
        userEventPublisher.publish(UserOperation.CREATED, created.email());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        return userRepository.findById(id).map(UserMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public Optional<UserDto> update(Long id, UserDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("dto не должен быть null");
        }
        UserDto normalized = normalize(dto);
        validate(normalized);
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        String email = normalized.email();
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ValidationException(List.of(EMAIL_TAKEN.formatted(email)));
        }
        User entity = existing.get();
        entity.setName(normalized.name());
        entity.setEmail(email);
        entity.setAge(normalized.age());
        try {
            return Optional.of(UserMapper.toDto(userRepository.save(entity)));
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(List.of(EMAIL_TAKEN.formatted(email)), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        userRepository.deleteById(id);
        userEventPublisher.publish(UserOperation.DELETED, existing.get().getEmail());
        return true;
    }

    private void validate(UserDto dto) {
        Set<ConstraintViolation<UserDto>> violations = VALIDATOR.validate(dto);
        if (violations.isEmpty()) {
            return;
        }
        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .distinct()
                .toList();
        throw new ValidationException(messages);
    }

    private UserDto normalize(UserDto dto) {
        String name = dto.name() == null ? null : dto.name().trim();
        String email = dto.email() == null
                ? null
                : dto.email().trim().toLowerCase(Locale.ROOT);
        return new UserDto(dto.id(), name, email, dto.age(), dto.createdAt());
    }
}
