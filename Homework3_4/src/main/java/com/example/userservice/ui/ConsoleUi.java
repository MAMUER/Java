package com.example.userservice.ui;

import com.example.userservice.dto.UserDto;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUi {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Scanner scanner;
    private final PrintStream out;

    @SuppressWarnings("java:S106")
    public ConsoleUi(Scanner scanner) {
        this(scanner, System.out);
    }

    public ConsoleUi(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    public void printMenu() {
        out.println("=== User Service ===");
        out.println("1. Создать пользователя");
        out.println("2. Найти по ID");
        out.println("3. Показать всех");
        out.println("4. Обновить пользователя");
        out.println("5. Удалить пользователя");
        out.println("0. Выход");
        out.print("Выберите действие: ");
    }

    public Optional<Integer> readChoice() {
        String line = readLine();
        if (line == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(line.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Long> readLong(String prompt) {
        out.print(prompt);
        String line = readLine();
        if (line == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(line.trim()));
        } catch (NumberFormatException e) {
            out.println("Ошибка: введите целое число.");
            return Optional.empty();
        }
    }

    public String readString(String prompt) {
        out.print(prompt);
        String line = readLine();
        return line == null ? "" : line.trim();
    }

    public Optional<Integer> readInt(String prompt) {
        out.print(prompt);
        String line = readLine();
        if (line == null) {
            return Optional.empty();
        }
        String trimmed = line.trim();
        Optional<Integer> parsed = tryParseAge(trimmed);
        if (parsed.isPresent()) {
            return parsed;
        }
        if (trimmed.matches("-?\\d+")) {
            out.println("Ошибка: число слишком большое.");
        } else {
            out.println("Ошибка: введите целое положительное число.");
        }
        return Optional.empty();
    }

    public static Optional<Integer> tryParseAge(String input) {
        String trimmed = input.trim();
        try {
            long value = Long.parseLong(trimmed);
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                return Optional.empty();
            }
            return Optional.of((int) value);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public void printUser(UserDto dto) {
        String createdAt = dto.createdAt() == null
                ? "-"
                : dto.createdAt().format(DATE_TIME_FORMATTER);
        out.printf("id=%d, name=%s, email=%s, age=%d, createdAt=%s%n",
                dto.id(), dto.name(), dto.email(), dto.age(), createdAt);
    }

    public void printUsers(java.util.List<UserDto> users) {
        if (users.isEmpty()) {
            out.println("Пользователи не найдены.");
            return;
        }
        users.forEach(this::printUser);
    }

    public void printMessage(String message) {
        out.println(message);
    }

    public void printError(String message) {
        out.println("Ошибка: " + message);
    }

    private String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }
}
