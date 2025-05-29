package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

/**
 * Класс предварительной обработки и валидации сущностей {@link User} на уровне сервиса
 */
@Slf4j
@Service
public class UserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public UserService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    /**
     * Метод возвращает коллекцию {@link User}
     *
     * @return коллекцию {@link User}
     */
    public Collection<User> findAll() {
        log.debug("Запрос всех пользователей на уровне сервиса");

        Collection<User> users = userStorage.findAll();

        log.debug("Возврат результатов поиска на уровень контроллера");
        return users;
    }

    public Collection<User> findFriends(Long userId) {
        log.debug("Запрос друзей пользователя на уровне сервиса");

        log.debug("Поиск пользователя по id {}", userId);
        Optional<User> userOpt = findById(userId);

        log.debug("Возврат результатов на уровень контроллера");
        return userOpt.map(user -> user.getFriends().values()).orElseGet(List::of);
    }

    public Collection<User> findCommonFriends(Long userId, Long friendId) {
        log.debug("Запрос общих друзей двух пользователей на уровне сервиса");

        Collection<User> userFriends = findFriends(userId);

        Collection<User> friendFriends = findFriends(friendId);

        log.debug("Возврат результатов поиска общих друзей на уровень контроллера");
        return userFriends.stream().filter(Objects::nonNull).filter(friendFriends::contains).toList();
    }

    /**
     * Метод возвращает экземпляр класса {@link User}, найденный по идентификатору
     *
     * @param userId идентификатор пользователя
     * @return экземпляр класса {@link User}
     * @throws ValidationException если передан пустой userId
     */
    public Optional<User> findById(Long userId) throws ValidationException {
        log.debug("Поиск пользователя по userId на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }

        Optional<User> userOpt = userStorage.findById(userId);

        if (userOpt.isEmpty()) {
            log.warn("Пользователь с id {} не найден в хранилище", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден в хранилище");
        }

        log.debug("Возврат результата поиска на уровень контроллера");
        return userOpt;
    }

    /**
     * Метод проверяет переданную модель {@link User} и передает для сохранения на уровень хранилища, после чего
     * сохранённую модель возвращает на уровень контроллера
     *
     * @param user несохранённый экземпляр класса {@link User}
     * @return сохраненный экземпляр класса {@link User}
     */
    public User create(User user) {
        log.debug("Создание пользователя на уровне сервиса");

        log.debug("Валидация переданной модели");
        validate(user);
        log.debug("Валидация модели завершена");

        log.debug("Сохранение данных в хранилище");
        user = userStorage.create(user);
        log.debug("Данные в хранилище сохранены");

        log.debug("Возврат результата добавления на уровень контроллера");
        return user;
    }

    /**
     * Метод проверяет переданную модель {@link User} и передает для обновления на уровень хранилища, после чего
     * сохранённую модель возвращает на уровень контроллера
     *
     * @param newUser несохраненный экземпляр класса {@link User}
     * @return сохраненный экземпляр класса {@link User}
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если пользователь для обновления не найден
     */
    public User update(User newUser) throws ValidationException, NotFoundException {
        log.debug("Обновление пользователя на уровне сервиса");

        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        Optional<User> existingUserOpt = userStorage.findById(newUser.getId());
        boolean valuesAreChanged = false;

        if (existingUserOpt.isEmpty()) {
            log.debug("Пользователь с id {} не найден в хранилище", newUser.getId());
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден в хранилище");
        }

        // Проверяем переданного пользователя
        validate(newUser);

        User existingUser = existingUserOpt.get();

        // Проверяем изменение электронной почты
        if (!newUser.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            log.debug("Будет изменена электронная почта пользователя с {} на {}", existingUser.getEmail(),
                    newUser.getEmail());
            existingUser.setEmail(newUser.getEmail());
            valuesAreChanged = true;
        }

        // Проверяем изменение логина
        if (!newUser.getLogin().equals(existingUser.getLogin())) {
            log.debug("Будет изменён логин пользователя с {} на {}", existingUser.getLogin(), newUser.getLogin());
            existingUser.setLogin(newUser.getLogin());
            valuesAreChanged = true;
        }

        // Проверяем изменение отображаемого имени
        if (!newUser.getName().equals(existingUser.getName())) {
            log.debug("Будет изменено отображаемое имя пользователя с {} на {}", existingUser.getName(),
                    newUser.getName());
            existingUser.setName(newUser.getName());
            valuesAreChanged = true;
        }

        // Проверяем изменение даты рождения
        if (!newUser.getBirthday().equals(existingUser.getBirthday())) {
            log.debug("Будет изменена дата рождения пользователя с {} на {}",
                    existingUser.getBirthday().format(DATE_FORMATTER), newUser.getBirthday().format(DATE_FORMATTER));
            existingUser.setBirthday(newUser.getBirthday());
            valuesAreChanged = true;
        }

        // Если изменения данных были
        if (valuesAreChanged) {
            // Проводим валидацию с учётом изменений
            log.debug("Валидация обновлённой модели");
            validate(existingUser);
            log.debug("Валидация обновлённой модели завершена");

            // Сохраняем изменения
            log.debug("Сохраняем изменения");
            userStorage.update(existingUser);
            log.info("Изменения сохранены");
        } else {
            log.info("Изменения не обнаружены");
        }

        log.debug("Возврат результата обновления на уровень контроллера");
        return existingUser;
    }

    /**
     * Метод добавляет друга в коллекцию друзей пользователя и пользователя в коллекцию друзей друга
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     * @throws RuntimeException при неожиданной ошибке
     */
    public User addFriend(Long userId, Long friendId) throws RuntimeException {
        log.debug("Добавление друзей на уровне сервиса");

        User user = null;
        User friend = null;

        // Получаем основного пользователя
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        }

        // Получаем добавляемого друга
        Optional<User> friendOpt = findById(friendId);
        if (friendOpt.isPresent()) {
            friend = friendOpt.get();
        }

        // Если все пользователи успешно получены
        if (user != null && friend != null) {
            // Добавляем пользователю друга в друзья
            log.debug("Добавляем друга с id {} в коллекцию пользователя с id {}", friendId, userId);
            user.addFriend(friend);

            log.debug("Сохраняем изменение пользователя в хранилище");
            userStorage.save(user);

            // Добавляем другу пользователя в друзья
            log.debug("Добавляем пользователя с id {} в коллекцию друга с id {}", userId, friendId);
            friend.addFriend(user);

            log.debug("Сохраняем изменение друга в хранилище");
            userStorage.save(friend);
        } else {
            log.warn("Во время добавления в друзья произошла непредвиденная ошибка");
            throw new RuntimeException("Во время добавления в друзья произошла непредвиденная ошибка");
        }

        log.debug("Возвращаем результат добавления на уровень контроллера");
        return friend;
    }

    /**
     * Метод удаляет друга из друзей пользователя и пользователя из друзей друга
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление друзей на уровне сервиса");

        User user = null;
        User friend = null;

        // Получаем основного пользователя
        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        }

        //Получаем удаляемого друга
        Optional<User> friendOpt = findById(friendId);
        if (friendOpt.isPresent()) {
            friend = friendOpt.get();
        }

        // Если все пользователи успешно получены
        if (user != null && friend != null) {
            // Удаляем из друзей пользователя друга
            log.debug("Удаляем друга с id {} из друзей пользователя с id {}", friendId, userId);
            user.removeFriend(friend.getId());

            log.debug("Сохраняем изменение друзей пользователя в хранилище");
            userStorage.save(user);

            // Удаляем из друзей друга пользователя
            friend.removeFriend(user.getId());

            log.debug("Сохраняем изменение друзей друга в хранилище");
            userStorage.save(friend);
        } else {
            log.warn("Во время удаления из друзей произошла непредвиденная ошибка");
            throw new RuntimeException("Во время удаления из друзей произошла непредвиденная ошибка");
        }

        log.debug("Возвращаем результат удаления на уровень контроллера");
    }

    /**
     * Метод удаляет {@link User} и все его связи из хранилища
     *
     * @param userId идентификатор пользователя
     */
    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя на уровне сервиса");

        if (userId == null) {
            log.warn("Передан пустой id");
            throw new ValidationException("Передан пустой id");
        }

        // Получаем пользователя из хранилища
        Optional<User> userOpt = userStorage.findById(userId);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            log.warn("Пользователь с id {} не найден в хранилище ", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        // Проверяем наличие лайков
        if (!user.getLikes().isEmpty()) {
            // Если лайки есть
            log.debug("У пользователя есть коллекция любимых фильмов");
            for (Film film : user.getLikes().values()) {
                // Удаляем каждый лайк с фильма
                film.removeUsersLike(user.getId());
                log.debug("Фильм с id {} больше не нравится пользователю", film.getId());

                // И сохраняем изменение фильма
                filmStorage.save(film);
            }
        }

        // Проверяем наличие друзей
        if (!user.getFriends().isEmpty()) {
            // Если друзья есть
            log.debug("У пользователя есть друзья");
            for (User friend : user.getFriends().values()) {
                // Удаляем друга
                friend.removeFriend(user.getId());
                log.debug("Пользователь больше не дружит с пользователем с id {}", friend.getId());

                // Сохраняем изменения
                userStorage.save(friend);
            }
        }

        // Удаляем пользователя
        userStorage.delete(user.getId());

        log.debug("Возврат результата удаления на уровень контроллера");
    }

    public void clearUsers() {
        log.debug("Очистка списка пользователей на уровне сервиса");

        // Получаем все фильмы с лайками
        for (Film film : filmStorage.findAll().stream().filter(f -> !f.getLikes().isEmpty()).toList()) {
            // Очищаем лайки
            film.getLikes().clear();
            log.debug("Фильм с id {} больше никому не нравится", film.getId());

            // Сохраняем изменения
            filmStorage.save(film);
        }

        // Очищаем хранилище
        userStorage.clear();

        log.debug("Возврат результата очистки на уровень контроллера");
    }

    /**
     * Валидация сущности {@link User} на правильное заполнение ключевых полей
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validate(User user) throws ValidationException {
        // Валидация электронной почты
        log.debug("Запускаем валидацию электронной почты");
        validateEmail(user);
        log.debug("Валидация электронной успешно почты завершена");

        // Валидация логина
        log.debug("Запускаем валидацию логина");
        validateLogin(user);
        log.debug("Валидация логина успешно завершена");

        // Валидация даты рождения
        log.debug("Запускаем валидацию даты рождения");
        validateBirthday(user);
        log.debug("Валидация даты рождения успешно завершена");
    }

    /**
     * Валидация электронной почты сущности {@link User}
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateEmail(User user) throws ValidationException {
        // Почта не должна быть пустой
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Передан пустой почтовый адрес");
            throw new ValidationException("Передан пустой почтовый адрес");
        }

        // Почта должна соответствовать формату e-mail
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(user.getEmail());

        if (!matcher.matches()) {
            log.debug("Передан неправильный почтовый адрес {}", user.getEmail());
            throw new ValidationException("Передан неправильный почтовый адрес: " + user.getEmail());
        }

        // Почта не должна быть ранее использована
        boolean exists = userStorage.isMailAlreadyUsed(user);

        if (exists) {
            log.warn("Электронная почта {} уже используется другим пользователем", user.getEmail());
            throw new ValidationException("Электронная почта " + user.getEmail() + " уже используется");
        }
    }

    /**
     * Валидация логина сущности {@link User}
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateLogin(User user) throws ValidationException {
        // Логин не должен быть пустой
        if (user.getLogin() == null) {
            throw new ValidationException("Передан пустой логин");
        }

        // Логин не должен содержать пробелы
        if (user.getLogin().contains(" ")) {
            log.debug("Передан логин ({}), который содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не должен содержать пробелы: " + user.getLogin());
        }

        // Логин не должен быть ранее использован
        boolean exists = userStorage.isLoginAlreadyUsed(user);

        if (exists) {
            log.warn("Логин {} уже используется другим пользователем", user.getLogin());
            throw new ValidationException("Логин " + user.getLogin() + " уже используется");
        }
    }

    /**
     * Валидация даты рождения сущности {@link User}
     *
     * @param user экземпляр класса {@link User}
     */
    private void validateBirthday(User user) {
        if (user.getBirthday() == null) {
            log.warn("Передана пустая дата рождения");
            throw new ValidationException("Дата рождения должна быть указана");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Передана дата рождения из будущего: {}", user.getBirthday().format(DATE_FORMATTER));
            throw new ValidationException("Дата рождения должна быть меньше текущей даты");
        }
    }
}
