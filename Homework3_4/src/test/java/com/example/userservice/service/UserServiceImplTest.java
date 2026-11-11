package com.example.userservice.service;

import com.example.userservice.dao.UserDao;
import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.DaoException;
import com.example.userservice.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createNormalizesNameAndEmailAndPersistsUser() {
        when(userDao.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(userDao.create(any(UserDto.class)))
                .thenAnswer(invocation -> {
                    UserDto argument = invocation.getArgument(0);
                    return new UserDto(42L, argument.name(), argument.email(),
                            argument.age(), CREATED_AT);
                });

        UserDto created = userService.create(
                new UserDto(null, "  Ivan  ", " Ivan@Example.COM ", 30, null));

        assertEquals(42L, created.id());
        assertEquals("Ivan", created.name());
        assertEquals("ivan@example.com", created.email());

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(userDao).create(captor.capture());
        assertNull(captor.getValue().id());
        assertEquals("ivan@example.com", captor.getValue().email());
    }

    @Test
    void createRejectsNullDto() {
        assertThrows(IllegalArgumentException.class, () -> userService.create(null));

        verifyNoInteractions(userDao);
    }

    @Test
    void createRejectsInvalidDto() {
        UserDto invalid = new UserDto(null, " ", "ivan@example.com", 30, null);

        assertThrows(ValidationException.class, () -> userService.create(invalid));

        verifyNoInteractions(userDao);
    }

    @Test
    void createRejectsDuplicateEmail() {
        UserDto duplicate = new UserDto(null, "Ivan", "ivan@example.com", 30, null);
        when(userDao.findByEmail("ivan@example.com"))
                .thenReturn(Optional.of(new UserDto(7L, "Ivan", "ivan@example.com", 30, CREATED_AT)));

        assertThrows(ValidationException.class, () -> userService.create(duplicate));

        verify(userDao, never()).create(any(UserDto.class));
    }

    @Test
    void findByIdDelegatesToDao() {
        UserDto stored = new UserDto(1L, "Ivan", "ivan@example.com", 30, CREATED_AT);
        when(userDao.findById(1L)).thenReturn(Optional.of(stored));

        Optional<UserDto> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(stored, result.get());
        verify(userDao).findById(1L);
    }

    @Test
    void findByIdReturnsEmptyWhenUserIsMissing() {
        when(userDao.findById(2L)).thenReturn(Optional.empty());

        assertTrue(userService.findById(2L).isEmpty());
    }

    @Test
    void findByIdRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> userService.findById(null));

        verifyNoInteractions(userDao);
    }

    @Test
    void findAllReturnsUnmodifiableSnapshot() {
        List<UserDto> mutable = new ArrayList<>();
        mutable.add(new UserDto(1L, "Ivan", "ivan@example.com", 30, CREATED_AT));
        when(userDao.findAll()).thenReturn(mutable);

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
        mutable.add(new UserDto(2L, "Anna", "anna@example.com", 25, CREATED_AT));
        assertEquals(1, result.size());
        assertThrows(UnsupportedOperationException.class, () -> result.add(null));
    }

    @Test
    void updateNormalizesInputAndKeepsCreatedAt() {
        UserDto existing = new UserDto(5L, "Ivan", "ivan@example.com", 30, CREATED_AT);
        UserDto updated = new UserDto(5L, "Ivan Petrov", "ivan.petrov@example.com", 31, CREATED_AT);
        when(userDao.findById(5L)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail("ivan.petrov@example.com")).thenReturn(Optional.empty());
        when(userDao.update(eq(5L), any(UserDto.class))).thenReturn(Optional.of(updated));

        Optional<UserDto> result = userService.update(5L,
                new UserDto(null, " Ivan Petrov ", " Ivan.Petrov@Example.com ", 31, null));

        assertTrue(result.isPresent());
        assertEquals("Ivan Petrov", result.get().name());

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(userDao).update(eq(5L), captor.capture());
        assertEquals("ivan.petrov@example.com", captor.getValue().email());
        assertEquals(CREATED_AT, captor.getValue().createdAt());
    }

    @Test
    void updateReturnsEmptyWhenUserIsMissing() {
        when(userDao.findById(9L)).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.update(9L,
                new UserDto(null, "Ivan", "ivan@example.com", 30, null));

        assertTrue(result.isEmpty());
        verify(userDao, never()).update(anyLong(), any(UserDto.class));
    }

    @Test
    void updateRejectsEmailOwnedByAnotherUser() {
        UserDto existing = new UserDto(3L, "Ivan", "ivan@example.com", 30, CREATED_AT);
        UserDto other = new UserDto(4L, "Anna", "anna@example.com", 25, CREATED_AT);
        when(userDao.findById(3L)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail("anna@example.com")).thenReturn(Optional.of(other));

        UserDto rename = new UserDto(null, "Ivan", "anna@example.com", 30, null);

        assertThrows(ValidationException.class, () -> userService.update(3L, rename));

        verify(userDao, never()).update(anyLong(), any(UserDto.class));
    }

    @Test
    void updateAllowsKeepingOwnEmail() {
        UserDto existing = new UserDto(3L, "Ivan", "ivan@example.com", 30, CREATED_AT);
        when(userDao.findById(3L)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail("ivan@example.com")).thenReturn(Optional.of(existing));
        when(userDao.update(eq(3L), any(UserDto.class))).thenReturn(Optional.of(existing));

        Optional<UserDto> result = userService.update(3L,
                new UserDto(null, "Ivan", "ivan@example.com", 31, null));

        assertTrue(result.isPresent());
    }

    @Test
    void updateRejectsNullArguments() {
        UserDto rename = new UserDto(null, "Ivan", "ivan@example.com", 30, null);

        assertThrows(IllegalArgumentException.class, () -> userService.update(null, rename));
        assertThrows(IllegalArgumentException.class, () -> userService.update(1L, null));

        verifyNoInteractions(userDao);
    }

    @Test
    void deleteDelegatesToDaoAndReportsResult() {
        when(userDao.delete(6L)).thenReturn(true);
        when(userDao.delete(7L)).thenReturn(false);

        assertTrue(userService.delete(6L));
        assertFalse(userService.delete(7L));
    }

    @Test
    void deleteRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> userService.delete(null));

        verifyNoInteractions(userDao);
    }

    @Test
    void daoFailuresArePropagated() {
        when(userDao.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(userDao.create(any(UserDto.class)))
                .thenThrow(new DaoException("Failed to create user", new RuntimeException()));

        UserDto create = new UserDto(null, "Ivan", "ivan@example.com", 30, null);

        assertThrows(DaoException.class, () -> userService.create(create));
    }
}