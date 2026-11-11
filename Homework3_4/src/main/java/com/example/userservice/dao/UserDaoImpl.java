package com.example.userservice.dao;

import com.example.userservice.config.HibernateUtil;
import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.DaoException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.hibernate.SessionFactory;

public class UserDaoImpl implements UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    private final SessionFactory sessionFactory;

    public UserDaoImpl() {
        this(HibernateUtil.getSessionFactory());
    }

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public UserDto create(UserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto не должен быть null");
        }
        UserMapper.validate(dto);
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User entity = UserMapper.toEntity(dto);
                session.persist(entity);
                tx.commit();
                LOGGER.info("Создан пользователь с id {}", entity.getId());
                return UserMapper.toDto(entity);
            } catch (Exception e) {
                rollbackQuietly(tx);
                throw new DaoException("Не удалось создать пользователя", e);
            }
        }
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User entity = session.get(User.class, id);
                tx.commit();
                return Optional.ofNullable(entity).map(UserMapper::toDto);
            } catch (Exception e) {
                rollbackQuietly(tx);
                throw new DaoException("Не удалось найти пользователя по id " + id, e);
            }
        }
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email не должен быть null");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User entity = session.createQuery(
                                "from User u where lower(u.email) = :email", User.class)
                        .setParameter("email", normalized)
                        .uniqueResult();
                tx.commit();
                return Optional.ofNullable(entity).map(UserMapper::toDto);
            } catch (Exception e) {
                rollbackQuietly(tx);
                throw new DaoException("Не удалось найти пользователя по email " + normalized, e);
            }
        }
    }

    @Override
    public List<UserDto> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                List<User> entities = session.createQuery(
                        "from User u order by u.id", User.class).getResultList();
                tx.commit();
                return entities.stream().map(UserMapper::toDto).toList();
            } catch (Exception e) {
                rollbackQuietly(tx);
                throw new DaoException("Не удалось получить список пользователей", e);
            }
        }
    }

    @Override
    public Optional<UserDto> update(Long id, UserDto dto) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("dto не должен быть null");
        }
        UserMapper.validate(dto);
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User entity = session.get(User.class, id);
                if (entity == null) {
                    tx.commit();
                    LOGGER.warn("Пользователь не найден для обновления, id {}", id);
                    return Optional.empty();
                }
                entity.setName(dto.name().trim());
                entity.setEmail(dto.email().trim());
                entity.setAge(dto.age());
                session.merge(entity);
                tx.commit();
                LOGGER.info("Обновлён пользователь с id {}", id);
                return Optional.of(UserMapper.toDto(entity));
            } catch (Exception e) {
                rollbackQuietly(tx);
                throw new DaoException("Не удалось обновить пользователя с id " + id, e);
            }
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не должен быть null");
        }
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                User entity = session.get(User.class, id);
                if (entity == null) {
                    tx.commit();
                    LOGGER.warn("Пользователь не найден для удаления, id {}", id);
                    return false;
                }
                session.remove(entity);
                tx.commit();
                LOGGER.info("Удалён пользователь с id {}", id);
                return true;
            } catch (Exception e) {
                rollbackQuietly(tx);
                throw new DaoException("Не удалось удалить пользователя с id " + id, e);
            }
        }
    }

    private void rollbackQuietly(Transaction tx) {
        try {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } catch (Exception rollbackEx) {
            LOGGER.error("Не удалось откатить транзакцию", rollbackEx);
        }
    }
}
