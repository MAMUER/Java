package com.example.userservice;

import com.example.userservice.config.HibernateUtil;
import com.example.userservice.dao.UserDao;
import com.example.userservice.dao.UserDaoImpl;
import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.DaoException;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.ui.ConsoleUi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String DB_ERROR_PREFIX = "Ошибка базы данных: ";
    private static final String USER_NOT_FOUND = "Пользователь не найден.";

    public static void main(String[] args) {
        if (args.length > 0 && "--example".equals(args[0])) {
            printExample();
            return;
        }
        LOGGER.info("Application started");
        Scanner scanner = new Scanner(System.in);

        ConsoleUi ui = null;
        try (scanner) {
            ui = new ConsoleUi(scanner);
            UserDao userDao = new UserDaoImpl();
            runLoop(ui, userDao);
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
            if (ui != null) {
                ui.printError("Непредвиденная ошибка: " + e.getMessage());
            } else {
                LOGGER.info("Непредвиденная ошибка: {}", e.getMessage());
            }
        } finally {
            HibernateUtil.shutdown();
            LOGGER.info("Application stopped");
        }
    }

    private static void runLoop(ConsoleUi ui, UserDao userDao) {
        boolean running = true;
        while (running) {
            ui.printMenu();
            Optional<Integer> choice = ui.readChoice();
            if (choice.isEmpty()) {
                ui.printError("Неверный пункт. Введите 0-5.");
                continue;
            }
            switch (choice.get()) {
                case 1 -> createUser(ui, userDao);
                case 2 -> findUserById(ui, userDao);
                case 3 -> showAllUsers(ui, userDao);
                case 4 -> updateUser(ui, userDao);
                case 5 -> deleteUser(ui, userDao);
                case 0 -> running = false;
                default -> ui.printError("Неверный пункт. Введите 0-5.");
            }
        }
    }

    private static void createUser(ConsoleUi ui, UserDao userDao) {
        try {
            String name;
            while (true) {
                name = ui.readString("Имя: ");
                List<String> nameViolations = UserMapper.validateName(name);
                if (nameViolations.isEmpty()) {
                    break;
                }
                printValidation(ui, nameViolations);
            }

            String email;
            while (true) {
                email = ui.readString("Email: ");
                List<String> emailViolations = UserMapper.validateEmail(email);
                if (emailViolations.isEmpty()) {
                    break;
                }
                printValidation(ui, emailViolations);
            }

            int age = 0;
            boolean ageValid = false;
            while (!ageValid) {
                Optional<Integer> ageOpt = ui.readInt("Возраст: ");
                if (ageOpt.isPresent()) {
                    List<String> ageViolations = UserMapper.validateAge(ageOpt.get());
                    if (ageViolations.isEmpty()) {
                        age = ageOpt.get();
                        ageValid = true;
                    } else {
                        printValidation(ui, ageViolations);
                    }
                }
            }

            UserDto dto = new UserDto(null, name.trim(), email.trim(), age, null);
            UserDto created = userDao.create(dto);
            ui.printMessage("Пользователь создан:");
            ui.printUser(created);
        } catch (ValidationException e) {
            printValidation(ui, e.getViolations());
        } catch (DaoException e) {
            printDbError(ui, e);
        }
    }

    private static void findUserById(ConsoleUi ui, UserDao userDao) {
        Optional<Long> idOpt = ui.readLong("ID: ");
        if (idOpt.isEmpty()) {
            return;
        }
        try {
            Optional<UserDto> result = userDao.findById(idOpt.get());
            if (result.isPresent()) {
                ui.printUser(result.get());
            } else {
                ui.printMessage(USER_NOT_FOUND);
            }
        } catch (DaoException e) {
            printDbError(ui, e);
        }
    }

    private static void showAllUsers(ConsoleUi ui, UserDao userDao) {
        try {
            List<UserDto> users = userDao.findAll();
            ui.printUsers(users);
        } catch (DaoException e) {
            printDbError(ui, e);
        }
    }

    private static void updateUser(ConsoleUi ui, UserDao userDao) {
        Optional<Long> idOpt = ui.readLong("ID: ");
        if (idOpt.isEmpty()) {
            return;
        }
        try {
            Optional<UserDto> existing = userDao.findById(idOpt.get());
            if (existing.isEmpty()) {
                ui.printMessage(USER_NOT_FOUND);
                return;
            }
            ui.printMessage("Текущие данные:");
            ui.printUser(existing.get());

            String name = readUpdatedName(ui, existing.get().name());
            String email = readUpdatedEmail(ui, existing.get().email());
            int age = readUpdatedAge(ui, existing.get().age());

            UserDto dto = new UserDto(null, name, email, age, existing.get().createdAt());
            Optional<UserDto> updated = userDao.update(idOpt.get(), dto);
            if (updated.isPresent()) {
                ui.printMessage("Пользователь обновлён:");
                ui.printUser(updated.get());
            } else {
                ui.printMessage(USER_NOT_FOUND);
            }
        } catch (ValidationException e) {
            printValidation(ui, e.getViolations());
        } catch (DaoException e) {
            printDbError(ui, e);
        }
    }

    private static String readUpdatedName(ConsoleUi ui, String current) {
        while (true) {
            String input = ui.readString("Новое имя (пусто - оставить): ");
            if (input.isBlank()) {
                return current;
            }
            List<String> violations = UserMapper.validateName(input);
            if (violations.isEmpty()) {
                return input.trim();
            }
            printValidation(ui, violations);
        }
    }

    private static String readUpdatedEmail(ConsoleUi ui, String current) {
        while (true) {
            String input = ui.readString("Новый email (пусто - оставить): ");
            if (input.isBlank()) {
                return current;
            }
            List<String> violations = UserMapper.validateEmail(input);
            if (violations.isEmpty()) {
                return input.trim();
            }
            printValidation(ui, violations);
        }
    }

    private static int readUpdatedAge(ConsoleUi ui, int current) {
        while (true) {
            String input = ui.readString("Новый возраст (0 - оставить): ");
            if (input.isBlank()) {
                return current;
            }
            String trimmed = input.trim();
            Optional<Integer> parsedOpt = ConsoleUi.tryParseAge(trimmed);
            if (parsedOpt.isPresent()) {
                int parsed = parsedOpt.get();
                if (parsed == 0) {
                    return current;
                }
                List<String> violations = UserMapper.validateAge(parsed);
                if (violations.isEmpty()) {
                    return parsed;
                }
                printValidation(ui, violations);
            } else if (trimmed.matches("-?\\d+")) {
                ui.printError("Число слишком большое.");
            } else {
                ui.printError("Введите целое положительное число.");
            }
        }
    }

    private static void deleteUser(ConsoleUi ui, UserDao userDao) {
        Optional<Long> idOpt = ui.readLong("ID: ");
        if (idOpt.isEmpty()) {
            return;
        }
        try {
            boolean deleted = userDao.delete(idOpt.get());
            if (deleted) {
                ui.printMessage("Пользователь удалён.");
            } else {
                ui.printMessage(USER_NOT_FOUND);
            }
        } catch (DaoException e) {
            printDbError(ui, e);
        }
    }

    private static void printDbError(ConsoleUi ui, DaoException e) {
        ui.printError(DB_ERROR_PREFIX + e.getMessage());
    }

    private static void printValidation(ConsoleUi ui, List<String> violations) {
        ui.printError(String.join("; ", violations));
    }

    private static void printExample() {
        LOGGER.info("=== Пример работы user-service ===");
        LOGGER.info("Выберите действие: 1");
        LOGGER.info("Имя: Иван");
        LOGGER.info("Email: ivan@example.com");
        LOGGER.info("Возраст: 30");
        LOGGER.info("Пользователь создан:");
        LOGGER.info("id=1, name=Иван, email=ivan@example.com, age=30, createdAt=2026-01-01 12:00:00");
        LOGGER.info("Выберите действие: 2");
        LOGGER.info("ID: 1");
        LOGGER.info("id=1, name=Иван, email=ivan@example.com, age=30, createdAt=2026-01-01 12:00:00");
        LOGGER.info("Выберите действие: 0");
    }
}
