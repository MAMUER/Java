package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException("userDao не должен быть null");
        }
        this.userDao = userDao;
    }

    @Override
    public UserDto create(UserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto не должен быть null");
        }
        UserMapper.validate(dto);
        String name = dto.name().trim();
        String email = normalizeEmail(dto.email());
        ensureEmailIsFree(email, null);
        UserDto created = userDao.create(new UserDto(null, name, email, dto.age(), null));
        LOGGER.info("Создан пользователь с id {}", created.id());
        return created;
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        return userDao.findById(id);
    }

    @Override
    public List<UserDto> findAll() {
        return List.copyOf(userDao.findAll());
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
        Optional<UserDto> existing = userDao.findById(id);
        if (existing.isEmpty()) {
            LOGGER.warn("Пользователь не найден для обновления, id {}", id);
            return Optional.empty();
        }
        String name = dto.name().trim();
        String email = normalizeEmail(dto.email());
        ensureEmailIsFree(email, id);
        Optional<UserDto> updated = userDao.update(id,
                new UserDto(id, name, email, dto.age(), existing.get().createdAt()));
        LOGGER.info("Обновлён пользователь с id {}", id);
        return updated;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        boolean deleted = userDao.delete(id);
        if (deleted) {
            LOGGER.info("Удалён пользователь с id {}", id);
        } else {
            LOGGER.warn("Пользователь не найден для удаления, id {}", id);
        }
        return deleted;
    }

    private void ensureEmailIsFree(String email, Long excludedId) {
        Optional<UserDto> existing = userDao.findByEmail(email);
        if (existing.isPresent() && !existing.get().id().equals(excludedId)) {
            throw new ValidationException(List.of("пользователь с email " + email + " уже существует"));
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}