package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createNormalizesNameAndEmailAndPersistsUser() {
        when(userRepository.existsByEmailIgnoreCase("ivan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User entity = invocation.getArgument(0);
            entity.setId(42L);
            entity.setCreatedAt(CREATED_AT);
            return entity;
        });

        UserDto created = userService.create(
                new UserDto(null, "  Ivan  ", " Ivan@Example.COM ", 30, null));

        assertEquals(42L, created.id());
        assertEquals("Ivan", created.name());
        assertEquals("ivan@example.com", created.email());
        assertEquals(30, created.age());
        assertEquals(CREATED_AT, created.createdAt());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Ivan", captor.getValue().getName());
        assertEquals("ivan@example.com", captor.getValue().getEmail());
    }

    @Test
    void createRejectsNullDto() {
        assertThrows(IllegalArgumentException.class, () -> userService.create(null));

        verifyNoInteractions(userRepository);
    }

    @Test
    void createRejectsInvalidDto() {
        UserDto invalid = new UserDto(null, " ", "ivan@example.com", 30, null);

        assertThrows(ValidationException.class, () -> userService.create(invalid));

        verifyNoInteractions(userRepository);
    }

    @Test
    void createRejectsDuplicateEmail() {
        UserDto duplicate = new UserDto(null, "Ivan", "ivan@example.com", 30, null);
        when(userRepository.existsByEmailIgnoreCase("ivan@example.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.create(duplicate));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createConvertsUniqueConstraintViolationToValidationException() {
        when(userRepository.existsByEmailIgnoreCase("ivan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        UserDto dto = new UserDto(null, "Ivan", "ivan@example.com", 30, null);

        assertThrows(ValidationException.class, () -> userService.create(dto));
    }

    @Test
    void findByIdDelegatesToRepository() {
        User stored = user(1L, "Ivan", "ivan@example.com", 30);
        when(userRepository.findById(1L)).thenReturn(Optional.of(stored));

        Optional<UserDto> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
        assertEquals("Ivan", result.get().name());
    }

    @Test
    void findByIdReturnsEmptyWhenUserIsMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertTrue(userService.findById(2L).isEmpty());
    }

    @Test
    void findByIdRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> userService.findById(null));

        verifyNoInteractions(userRepository);
    }

    @Test
    void findAllReturnsMappedUsers() {
        when(userRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                user(1L, "Ivan", "ivan@example.com", 30),
                user(2L, "Anna", "anna@example.com", 25)));

        List<UserDto> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals(List.of("Ivan", "Anna"), result.stream().map(UserDto::name).toList());
    }

    @Test
    void updateChangesFieldsAndKeepsCreatedAt() {
        User existing = user(5L, "Ivan", "ivan@example.com", 30);
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("ivan.petrov@example.com", 5L))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserDto> result = userService.update(5L,
                new UserDto(null, " Ivan Petrov ", " Ivan.Petrov@Example.com ", 31, null));

        assertTrue(result.isPresent());
        assertEquals("Ivan Petrov", result.get().name());
        assertEquals("ivan.petrov@example.com", result.get().email());
        assertEquals(31, result.get().age());
        assertEquals(CREATED_AT, result.get().createdAt());
    }

    @Test
    void updateReturnsEmptyWhenUserIsMissing() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.update(9L,
                new UserDto(null, "Ivan", "ivan@example.com", 30, null));

        assertTrue(result.isEmpty());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRejectsEmailOwnedByAnotherUser() {
        User existing = user(3L, "Ivan", "ivan@example.com", 30);
        when(userRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("anna@example.com", 3L))
                .thenReturn(true);

        UserDto rename = new UserDto(null, "Ivan", "anna@example.com", 30, null);

        assertThrows(ValidationException.class, () -> userService.update(3L, rename));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRejectsNullArguments() {
        UserDto rename = new UserDto(null, "Ivan", "ivan@example.com", 30, null);

        assertThrows(IllegalArgumentException.class, () -> userService.update(null, rename));
        assertThrows(IllegalArgumentException.class, () -> userService.update(1L, null));

        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteRemovesExistingUser() {
        when(userRepository.existsById(6L)).thenReturn(true);

        assertTrue(userService.delete(6L));

        verify(userRepository).deleteById(6L);
    }

    @Test
    void deleteReturnsFalseForMissingUser() {
        when(userRepository.existsById(7L)).thenReturn(false);

        assertFalse(userService.delete(7L));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> userService.delete(null));

        verifyNoInteractions(userRepository);
    }

    private static User user(Long id, String name, String email, Integer age) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        user.setCreatedAt(CREATED_AT);
        return user;
    }
}
