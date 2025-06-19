package ru.yandex.practicum.filmorate.storage.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

/**
 * Класс обработки сущностей {@link User} на уровне хранилища. Реализация хранения данных в оперативной памяти.
 */
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final FilmStorage filmStorage;

    private final HashMap<Long, User> users = new HashMap<>();
    private long generatedId;

    @Autowired
    public InMemoryUserStorage(FilmStorage filmStorage) {
        log.debug("Будет использована реализация хранения пользователей в памяти приложения");

        this.filmStorage = filmStorage;
    }

    @Override
    public Collection<User> findAll(Integer size, Integer from) {
        log.debug("Запрос всех пользователей на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<User> result = users.values().stream()
                .skip(from)
                .limit(size)
                .toList();
        log.debug("Получена коллекция всех пользователей размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<User> findByFilmId(Long filmId) {
        log.debug("Запрос лайков на уровне хранилища");
        log.debug("Идентификатор запрашиваемого фильма: {}", filmId);

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден в хранилище"));
        log.debug("Получена коллекция идентификаторов пользователей размером {}", film.getLikes().size());

        Collection<User> result = users.values().stream()
                .filter(user -> film.getLikes().contains(user.getId()))
                .toList();
        log.debug("Получена коллекция пользователей размером {}", result.size());

        log.debug("Возврат лайков на уровень сервиса");
        return result;
    }

    @Override
    public Collection<User> findFriends(Long userId) {
        log.debug("Запрос друзей на уровне хранилища");
        log.debug("Идентификатор пользователя: {}", userId);

        User user = findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        log.debug("Получена коллекция идентификатором пользователей размером {}", user.getFriends().size());

        log.debug("Возврат результатов поиска друзей на уровень сервиса");
        return users.values().stream()
                .filter(friend -> user.getFriends().contains(friend.getId()))
                .toList();
    }

    @Override
    public Collection<User> findCommonFriends(Long userId, Long friendId) {
        log.debug("Запрос списка общих друзей на уровне хранилища");
        log.debug("Идентификатор первого пользователя: {}", userId);
        log.debug("Идентификатор второго пользователя: {}", friendId);

        User user = findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        User friend = findById(friendId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        Collection<Long> commonFriendIds = user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .toList();
        log.debug("Получена коллекция идентификаторов размером {}", commonFriendIds.size());

        log.debug("Возврат результатов поиска общих друзей на уровень сервиса");
        return users.values().stream()
                .filter(commonFriend -> commonFriendIds.contains(commonFriend.getId()))
                .toList();
    }

    @Override
    public Optional<User> findById(Long userId) {
        log.debug("Поиск пользователя по userId на уровне хранилища");
        log.debug("Идентификатор искомого пользователя: {}", userId);

        User user = users.get(userId);

        log.debug("Пользователь с userId {} {} в хранилище", userId, user == null ? "не найден" : "найден");

        log.debug("Возврат результата поиска на уровень сервиса");
        return Optional.ofNullable(user);
    }

    @Override
    public User createUser(User user) {
        log.debug("Создание пользователя на уровне хранилища");

        Long nextId = getNextId();
        log.debug("Сгенерировано значение {}", nextId);

        user.setId(nextId);
        log.debug("Значение присвоено id присвоено пользователю");

        save(user);

        log.debug("Возврат результатов создания на уровень сервиса");
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        log.debug("Добавление друга на уровне хранилища");
        log.debug("Идентификатор основного пользователя: {}", userId);
        log.debug("Идентификатор друга: {}", friendId);

        // Получаем пользователей из хранилища
        User user = users.get(userId);
        log.debug("В хранилище найден основной пользователь с id {}", user.getId());

        User friend = users.get(friendId);
        log.debug("В хранилище найден друг с id {}", friend.getId());

        user.getFriends().add(friend.getId());
        log.debug("Пользователь с id {} добавлен в друзья пользователю с id {}", friend.getId(), user.getId());
        save(user);

        friend.getFriends().add(userId);
        log.debug("Пользователь с id {} добавлен в друзья пользователю с id {}", user.getId(), friend.getId());
        save(friend);

        log.debug("Возврат результата добавления дружбы на уровень сервиса");
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление друга на уровне хранилища");
        log.debug("Идентификатор  основного пользователя: {}", userId);
        log.debug("Идентификатор  друга: {}", friendId);

        // Получаем пользователей из хранилища
        User user = users.get(userId);
        log.debug("В хранилище найден  основной пользователь с id {}", user.getId());

        User friend = users.get(friendId);
        log.debug("В хранилище найден  друг с id {}", friend.getId());

        user.getFriends().remove(friend.getId());
        log.debug("Пользователь с id {} удалён из друзей пользователя с id {}", friend.getId(), user.getId());
        save(user);

        friend.getFriends().remove(user.getId());
        log.debug("Пользователь с id {} удалён из друзей пользователя с id {}", user.getId(), friend.getId());
        save(friend);

        log.debug("Возврат результатов удаления дружбы на уровень сервиса");
    }

    public void save(User user) {
        log.debug("Сохранение пользователя в хранилище");

        users.put(user.getId(), user);

        log.debug("Сохранение пользователя завершено");
    }

    @Override
    public User updateUser(User newUser) {
        log.debug("Изменение пользователя на уровне хранилища");

        save(newUser);

        log.debug("Возврат результатов изменения на уровень сервиса");
        return newUser;
    }

    @Override
    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя на уровне хранилища");
        log.debug("Передан id пользователя: {}", userId);

        users.remove(userId);

        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearUsers() {
        log.debug("Очистка хранилища пользователей");

        users.clear();

        log.debug("Возврат результатов очистки на уровень сервиса");
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
