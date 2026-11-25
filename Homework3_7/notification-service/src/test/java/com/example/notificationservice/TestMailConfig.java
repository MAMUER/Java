package com.example.notificationservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestMailConfig {

    private static final String DEFAULT_MAIL_USER = "mihnikolaenko12@yandex.ru";
    private static final String MAIL_USERNAME_KEY = "MAIL_USERNAME=";
    private static final String[] ENV_FILES = {".env", "../.env"};

    public static final String MAIL_USER = resolveMailUser();

    private TestMailConfig() {
    }

    private static String resolveMailUser() {
        String fromEnvironment = System.getenv("MAIL_USERNAME");
        if (isNotBlank(fromEnvironment)) {
            return fromEnvironment.trim();
        }
        String fromEnvFile = resolveFromEnvFiles();
        return isNotBlank(fromEnvFile) ? fromEnvFile : DEFAULT_MAIL_USER;
    }

    private static String resolveFromEnvFiles() {
        for (String candidate : ENV_FILES) {
            Path path = Path.of(candidate);
            if (!Files.isRegularFile(path)) {
                continue;
            }
            String value = readMailUsername(path);
            if (isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static String readMailUsername(Path path) {
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.startsWith(MAIL_USERNAME_KEY)) {
                    return trimmed.substring(MAIL_USERNAME_KEY.length()).trim();
                }
            }
        } catch (IOException e) { // NOSONAR
            return null;
        }
        return null;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}