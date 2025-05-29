package ru.yandex.practicum.filmorate.storage.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

/**
 * Класс обработки сущностей {@link User} на уровне хранилища. Реализация хранения данных в оперативной памяти.
 */
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final HashMap<Long, User> users = new HashMap<>();
    private long generatedId;

    @Override
    public Collection<User> findAll() {
        log.debug("Запрос всех пользователей на уровне хранилища");

        Collection<User> result = users.values();

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Optional<User> findById(Long userId) {
        log.debug("Поиск пользователя по userId на уровне хранилища");
        User user = users.get(userId);

        log.debug("Пользователь с userId {} {} в хранилище", userId, user == null ? "не найден" : "найден");

        log.debug("Возврат результата поиска на уровень сервиса");
        return Optional.ofNullable(user);
    }

    @Override
    public void save(User user) {
        log.debug("Сохранение пользователя в хранилище");
        users.put(user.getId(), user);
        log.debug("Сохранение пользователя завершено");
    }

    @Override
    public User create(User user) {
        log.debug("Создание пользователя на уровне хранилища");

        user.setId(getNextId());

        save(user);

        log.debug("Возврат результатов создания на уровень сервиса");
        return user;
    }

    @Override
    public User update(User newUser) {
        log.debug("Изменение пользователя на уровне хранилища");

        save(newUser);

        log.debug("Возврат результатов изменения на уровень сервиса");
        return newUser;
    }

    @Override
    public void delete(Long userId) {
        log.debug("Удаление пользователя на уровне хранилища");

        users.remove(userId);

        log.info("Удаление пользователя завершено");
    }

    @Override
    public void clear() {
        log.debug("Очистка хранилища пользователей");

        users.clear();

        log.debug("Очистка хранилища завершена");
    }

    @Override
    public Boolean isMailAlreadyUsed(User user) {
        log.debug("Проверка на использование адреса {} другими пользователями", user.getEmail());
        return users.values().stream().anyMatch(
                existingUser -> !existingUser.getId().equals(user.getId()) && existingUser.getEmail()
                        .equalsIgnoreCase(user.getEmail()));
    }

    @Override
    public Boolean isLoginAlreadyUsed(User user) {
        log.debug("Проверка на использование логина {} другими пользователями", user.getLogin());
        return users.values().stream().anyMatch(
                existingUser -> !existingUser.getId().equals(user.getId()) && existingUser.getLogin()
                        .equalsIgnoreCase(user.getLogin()));
    }

    private Long getNextId() {
        log.debug("Генерация нового идентификатора для пользователя");
        return ++generatedId;
    }
}
