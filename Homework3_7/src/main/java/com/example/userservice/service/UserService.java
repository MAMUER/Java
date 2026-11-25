package com.example.userservice.service;

import com.example.userservice.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDto create(UserDto dto);

    Optional<UserDto> findById(Long id);

    List<UserDto> findAll();

    Optional<UserDto> update(Long id, UserDto dto);

    boolean delete(Long id);
}