package com.example.userservice.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HibernateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtil.class);

    private static final class Holder {
        private static final SessionFactory INSTANCE = buildSessionFactory();

        private static SessionFactory buildSessionFactory() {
            try {
                Configuration configuration = new Configuration().configure();

                String dbUrl = System.getenv("DB_URL");
                String dbUser = System.getenv("DB_USER");
                String dbPassword = System.getenv("DB_PASSWORD");

                if (dbUrl != null && !dbUrl.isBlank()) {
                    configuration.setProperty("hibernate.connection.url", dbUrl);
                }
                if (dbUser != null && !dbUser.isBlank()) {
                    configuration.setProperty("hibernate.connection.username", dbUser);
                }
                if (dbPassword != null) {
                    configuration.setProperty("hibernate.connection.password", dbPassword);
                }

                SessionFactory factory = configuration.buildSessionFactory();
                LOGGER.info("Hibernate SessionFactory created successfully");
                return factory;
            } catch (Exception e) {
                LOGGER.error("Failed to create SessionFactory", e);
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        return Holder.INSTANCE;
    }

    public static void shutdown() {
        if (Holder.INSTANCE != null && !Holder.INSTANCE.isClosed()) {
            Holder.INSTANCE.close();
            LOGGER.info("Hibernate SessionFactory closed");
        }
    }
}
