package com.example.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationServiceLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceLauncher.class);
    private static final int PORT = 8081;

    private final String jarPath;
    private final String workingDir;
    private final String javaBinary;

    public NotificationServiceLauncher(
            @Value("${app.notification-service.jar:notification-service/target/notification-service-1.0.0.jar}") String jarPath,
            @Value("${app.notification-service.dir:notification-service}") String workingDir) {
        this.jarPath = jarPath;
        this.workingDir = workingDir;
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        this.javaBinary = Path.of(System.getProperty("java.home"), "bin", executable).toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startNotificationService() {
        if (isPortInUse()) {
            LOGGER.info("notification-service already running on port {}", PORT);
            return;
        }
        Path jar = Path.of(jarPath).toAbsolutePath();
        if (!Files.isRegularFile(jar)) {
            LOGGER.warn("notification-service jar not found at {}, skipping auto-start", jar);
            return;
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(javaBinary, "-jar", jar.toString());
            builder.directory(Path.of(workingDir).toAbsolutePath().toFile());
            builder.inheritIO();
            Process process = builder.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }));
            LOGGER.info("notification-service started (pid {}), waiting for port {}", process.pid(), PORT);
            awaitPort(60, TimeUnit.SECONDS);
        } catch (IOException e) {
            LOGGER.error("Failed to start notification-service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Interrupted while waiting for notification-service", e);
        }
    }

    private boolean isPortInUse() {
        try (var socket = new java.net.ServerSocket(PORT)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private void awaitPort(int timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            try (var socket = new java.net.Socket()) {
                socket.connect(new InetSocketAddress("localhost", PORT), 500);
                LOGGER.info("notification-service is up on port {}", PORT);
                return;
            } catch (IOException ignored) {
                TimeUnit.SECONDS.sleep(2);
            }
        }
        LOGGER.warn("notification-service did not open port {} in time", PORT);
    }
}