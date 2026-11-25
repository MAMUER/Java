package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserRequest;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Tag(name = "Users", description = "Операции управления пользователями")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Список пользователей",
            description = "Возвращает всех пользователей и HATEOAS-ссылки для навигации")
    @GetMapping
    public List<EntityModel<UserDto>> findAll() {
        return userService.findAll().stream()
                .map(this::toModel)
                .toList();
    }

    @Operation(summary = "Найти пользователя по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public EntityModel<UserDto> findById(
            @Parameter(description = "ID пользователя") @PathVariable Long id) {
        return toModel(userService.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Operation(summary = "Создать пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserDto>> create(@Valid @RequestBody UserRequest request) {
        EntityModel<UserDto> model = toModel(userService.create(toDto(request)));
        return ResponseEntity.created(URI.create("/api/users/" + model.getContent().id()))
                .body(model);
    }

    @Operation(summary = "Обновить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/{id}")
    public EntityModel<UserDto> update(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return toModel(userService.update(id, toDto(request))
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        if (!userService.delete(id)) {
            throw new UserNotFoundException(id);
        }
    }

    private EntityModel<UserDto> toModel(UserDto dto) {
        return EntityModel.of(dto,
                linkTo(UserController.class).slash(dto.id()).withSelfRel(),
                linkTo(UserController.class).withRel("users"));
    }

    private UserDto toDto(UserRequest request) {
        return new UserDto(null, request.name(), request.email(), request.age(), null);
    }
}