package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createReturnsCreatedUserAndLocation() throws Exception {
        when(userService.create(new UserDto(null, "Ivan", "ivan@example.com", 30, null)))
                .thenReturn(new UserDto(42L, "Ivan", "ivan@example.com", 30, CREATED_AT));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ivan","email":"ivan@example.com","age":30}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.createdAt").value("2026-01-01T12:00:00"));
    }

    @Test
    void findByIdReturnsUser() throws Exception {
        when(userService.findById(1L))
                .thenReturn(Optional.of(new UserDto(1L, "Ivan", "ivan@example.com", 30, CREATED_AT)));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void findByIdReturnsNotFoundWhenUserIsMissing() throws Exception {
        when(userService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id 99 не найден"));
    }

    @Test
    void findAllReturnsUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                new UserDto(1L, "Ivan", "ivan@example.com", 30, CREATED_AT),
                new UserDto(2L, "Anna", "anna@example.com", 25, CREATED_AT)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ivan"))
                .andExpect(jsonPath("$[1].name").value("Anna"));
    }

    @Test
    void updateReturnsUpdatedUser() throws Exception {
        when(userService.update(1L,
                new UserDto(null, "Ivan Petrov", "ivan.petrov@example.com", 31, null)))
                .thenReturn(Optional.of(new UserDto(1L, "Ivan Petrov",
                        "ivan.petrov@example.com", 31, CREATED_AT)));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ivan Petrov","email":"ivan.petrov@example.com","age":31}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan Petrov"))
                .andExpect(jsonPath("$.email").value("ivan.petrov@example.com"))
                .andExpect(jsonPath("$.age").value(31));
    }

    @Test
    void updateReturnsNotFoundWhenUserIsMissing() throws Exception {
        when(userService.update(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ivan","email":"ivan@example.com","age":30}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id 99 не найден"));
    }

    @Test
    void deleteRemovesUser() throws Exception {
        when(userService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void deleteReturnsNotFoundWhenUserIsMissing() throws Exception {
        when(userService.delete(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id 99 не найден"));
    }

    @Test
    void createReturnsBadRequestWithValidationDetails() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ivan","email":"bad-email","age":30}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email не соответствует формату"))
                .andExpect(jsonPath("$.details[0]").value("email не соответствует формату"));
    }

    @Test
    void createReturnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserDto.class));
    }
}
