package com.example.userservice.dao;

import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.DaoException;
import com.example.userservice.exception.ValidationException;
import com.example.userservice.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class UserDaoImplIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("user_service_test")
                    .withUsername("test")
                    .withPassword("test");

    private static SessionFactory sessionFactory;

    private UserDao userDao;

    @BeforeAll
    static void buildSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", POSTGRES.getDriverClassName());
        configuration.setProperty("hibernate.connection.url", POSTGRES.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", POSTGRES.getUsername());
        configuration.setProperty("hibernate.connection.password", POSTGRES.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.setProperty("hibernate.format_sql", "false");
        configuration.addAnnotatedClass(User.class);
        sessionFactory = configuration.buildSessionFactory();
    }

    @BeforeEach
    void resetDatabase() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeMutationQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE")
                    .executeUpdate();
            transaction.commit();
        }
        userDao = new UserDaoImpl(sessionFactory);
    }

    @AfterAll
    static void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    @Test
    void createPersistsGeneratedIdAndTimestamp() {
        UserDto created = userDao.create(new UserDto(null, "Alice", "alice@example.com", 30, null));

        assertNotNull(created.id());
        assertEquals("Alice", created.name());
        assertEquals("alice@example.com", created.email());
        assertEquals(30, created.age());
        assertNotNull(created.createdAt());
    }

    @Test
    void findByIdReturnsStoredUser() {
        UserDto created = userDao.create(new UserDto(null, "Bob", "bob@example.com", 25, null));

        Optional<UserDto> found = userDao.findById(created.id());

        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
        assertEquals("bob@example.com", found.get().email());
    }

    @Test
    void findByIdReturnsEmptyForMissingUser() {
        assertTrue(userDao.findById(987654321L).isEmpty());
    }

    @Test
    void findAllReturnsOnlyExistingUsers() {
        userDao.create(new UserDto(null, "Carol", "carol@example.com", 41, null));
        userDao.create(new UserDto(null, "Dave", "dave@example.com", 19, null));

        List<UserDto> users = userDao.findAll();

        assertEquals(2, users.size());
        assertEquals(List.of("Carol", "Dave"), users.stream().map(UserDto::name).toList());
    }

    @Test
    void updateChangesStoredFieldsAndKeepsCreatedAt() {
        UserDto created = userDao.create(new UserDto(null, "Eve", "eve@example.com", 22, null));

        Optional<UserDto> updated = userDao.update(created.id(),
                new UserDto(null, "Evelyn", "evelyn@example.com", 23, null));

        assertTrue(updated.isPresent());
        assertEquals("Evelyn", updated.get().name());
        assertEquals("evelyn@example.com", updated.get().email());
        assertEquals(23, updated.get().age());
        assertEquals(created.createdAt(), updated.get().createdAt());
    }

    @Test
    void updateReturnsEmptyForMissingUser() {
        Optional<UserDto> updated = userDao.update(987654321L,
                new UserDto(null, "Ghost", "ghost@example.com", 30, null));

        assertTrue(updated.isEmpty());
    }

    @Test
    void deleteRemovesUser() {
        UserDto created = userDao.create(new UserDto(null, "Frank", "frank@example.com", 33, null));

        assertTrue(userDao.delete(created.id()));
        assertTrue(userDao.findById(created.id()).isEmpty());
    }

    @Test
    void deleteReturnsFalseForMissingUser() {
        assertFalse(userDao.delete(987654321L));
    }

    @Test
    void findByEmailIgnoresCaseAndSurroundingSpaces() {
        UserDto created = userDao.create(new UserDto(null, "Grace", "grace@example.com", 28, null));

        Optional<UserDto> found = userDao.findByEmail("  GRACE@example.com ");

        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
    }

    @Test
    void duplicateEmailViolatesUniqueConstraint() {
        userDao.create(new UserDto(null, " Heidi ", "heidi@example.com", 31, null));
        UserDto duplicate = new UserDto(null, "Heidi Two", "heidi@example.com", 35, null);

        assertThrows(DaoException.class, () -> userDao.create(duplicate));
    }

    @Test
    void createRejectsInvalidDto() {
        UserDto invalid = new UserDto(null, " ", "ivan@example.com", 30, null);

        assertThrows(ValidationException.class, () -> userDao.create(invalid));
    }

    @Test
    void createRejectsNullDto() {
        assertThrows(IllegalArgumentException.class, () -> userDao.create(null));
    }

    @Test
    void findByIdRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> userDao.findById(null));
    }

    @Test
    void transactionsAreCommittedAndDataSurvivesNewSession() {
        UserDto created = userDao.create(new UserDto(null, "Ivan", "ivan@example.com", 30, null));

        try (Session session = sessionFactory.openSession()) {
            User entity = session.get(User.class, created.id());
            assertNotNull(entity);
            assertEquals("ivan@example.com", entity.getEmail());
            assertNotNull(entity.getCreatedAt());
        }
    }
}