package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository не должен быть null");
        }
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto не должен быть null");
        }
        UserMapper.validate(dto);
        String email = normalizeEmail(dto.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException(List.of(
                    "пользователь с email " + email + " уже существует"));
        }
        User entity = UserMapper.toEntity(
                new UserDto(null, dto.name().trim(), email, dto.age(), null));
        try {
            return UserMapper.toDto(userRepository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(List.of(
                    "пользователь с email " + email + " уже существует"));
        }
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
        UserMapper.validate(dto);
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        String email = normalizeEmail(dto.email());
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ValidationException(List.of(
                    "пользователь с email " + email + " уже существует"));
        }
        User entity = existing.get();
        entity.setName(dto.name().trim());
        entity.setEmail(email);
        entity.setAge(dto.age());
        try {
            return Optional.of(UserMapper.toDto(userRepository.save(entity)));
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(List.of(
                    "пользователь с email " + email + " уже существует"));
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
