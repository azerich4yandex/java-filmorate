package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

/**
 * Класс предварительной обработки и валидации сущностей {@link User} на уровне сервиса
 */
@Slf4j
@Service
public class UserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Метод возвращает коллекцию {@link User}
     *
     * @return коллекцию {@link User}
     */
    public Collection<User> findAll() {
        log.debug("Запрос всех пользователей на уровне сервиса");

        Collection<User> result = userStorage.findAll();
        log.debug("На уровень сервиса вернулась коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link User}, найденный по идентификатору
     *
     * @param userId идентификатор пользователя
     * @return экземпляр класса {@link User}
     * @throws ValidationException если передан пустой userId
     */
    public User findById(Long userId) throws ValidationException {
        log.debug("Поиск пользователя по id на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }
        log.debug("Передан id пользователя: {}", userId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));
        log.debug("Найден пользователь с id {}", user.getId());

        log.debug("Возврат результата поиска на уровень контроллера");
        return user;
    }

    public Collection<User> findFriends(Long userId) {
        log.debug("Запрос друзей пользователя на уровне сервиса");

        if (userId == null) {
            throw new NotFoundException("Передан пустой userId");
        }
        log.debug("Передан userId пользователя: {}", userId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));
        log.debug("Найден пользователь с  id  {} ", user.getId());

        Collection<User> result = user.getFriends().stream().map(id -> userStorage.findById(id).orElse(null))
                .filter(Objects::nonNull).toList();
        log.debug("Получена коллекция друзей пользователя размером {}", result.size());

        log.debug("Возврат результатов на уровень контроллера");
        return result;
    }

    public Collection<User> findCommonFriends(Long userId, Long friendId) {
        log.debug("Запрос общих друзей двух пользователей на уровне сервиса");
        log.debug("Передан id первого пользователя: {}", userId);
        log.debug("Передан id второго пользователя: {}", friendId);

        // Получаем список друзей пользователя
        Collection<User> userFriends = findFriends(userId);

        // Получаем список друзей друга
        Collection<User> friendFriends = findFriends(friendId);

        // Получаем пересечение коллекций
        Collection<User> result = userFriends.stream().filter(Objects::nonNull).filter(friendFriends::contains)
                .toList();
        log.debug("Получен список общих друзей между пользователем с id {} и другом с id {} размером {}", userId,
                friendId, result.size());

        log.debug("Возврат результатов поиска общих друзей на уровень контроллера");
        return result;
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
        log.debug("Передан пользователь с id: {}", newUser.getId());

        // Получаем пользователя из хранилища
        User existingUser = userStorage.findById(newUser.getId()).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + newUser.getId() + " не найден в хранилище"));
        log.debug("В хранилище найден пользователь по id {}", existingUser.getId());

        // Проверяем переданного пользователя
        validate(newUser);

        // Флаг необходимости изменения данных
        boolean valuesAreChanged = false;

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
            userStorage.update(existingUser);
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
    public void addFriend(Long userId, Long friendId) throws RuntimeException {
        log.debug("Добавление друзей на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }

        if (friendId == null) {
            throw new ValidationException("Передан пустой friendId");
        }

        // Получаем основного пользователя
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Получаем добавляемого друга
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден в хранилище"));

        // Если все пользователи успешно получены
        if (user != null && friend != null) {
            // Добавляем пользователю друга в друзья
            log.debug("Добавляем друга с id {} в коллекцию пользователя с id {}", friendId, userId);
            user.getFriends().add(friend.getId());

            log.debug("Сохраняем изменение пользователя в хранилище");
            userStorage.save(user);

            // Добавляем другу пользователя в друзья
            log.debug("Добавляем пользователя с id {} в коллекцию друга с id {}", userId, friendId);
            friend.getFriends().add(user.getId());

            log.debug("Сохраняем изменение друга в хранилище");
            userStorage.save(friend);
        } else {
            throw new RuntimeException("Во время добавления в друзья произошла непредвиденная ошибка");
        }

        log.debug("Возвращаем результат добавления на уровень контроллера");
    }

    /**
     * Метод удаляет друга из друзей пользователя и пользователя из друзей друга
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление друзей на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }

        if (friendId == null) {
            throw new ValidationException("Передан пустой friendId");
        }

        // Проверяем наличие пользователя
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Проверяем наличие друга
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден в хранилище"));

        // Если все пользователи успешно получены
        if (user != null && friend != null) {
            // Удаляем из друзей пользователя друга
            log.debug("Удаляем друга с id {} из друзей пользователя с id {}", friendId, userId);
            user.getFriends().remove(friend.getId());

            log.debug("Сохраняем изменение друзей пользователя в хранилище");
            userStorage.save(user);

            // Удаляем из друзей друга пользователя
            friend.getFriends().remove(user.getId());

            log.debug("Сохраняем изменение друзей друга в хранилище");
            userStorage.save(friend);
        } else {
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
            throw new ValidationException("Передан пустой id");
        }

        // Получаем пользователя из хранилища
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));
        log.debug("В хранилище найден пользователь с  id  {} ", user.getId());

        // Получаем друзей пользователя
        Collection<Long> friends = user.getFriends().stream().toList();

        // Проверяем наличие друзей
        if (!friends.isEmpty()) {
            // Если друзья есть
            log.debug("У пользователя есть друзья");
            for (Long friendId : friends) {
                // Удаляем дружбу с пользователем
                removeFriend(friendId, user.getId());
            }
        }
        // Удаляем пользователя
        userStorage.delete(user.getId());

        log.debug("Возврат результата удаления на уровень контроллера");
    }

    public void clearUsers() {
        log.debug("Очистка списка пользователей на уровне сервиса");

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
        validateEmail(user);

        // Валидация логина
        validateLogin(user);

        // Валидация даты рождения
        validateBirthday(user);
    }

    /**
     * Валидация электронной почты сущности {@link User}
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateEmail(User user) throws ValidationException {
        log.debug("Запускаем валидацию электронной почты");
        // Почта не должна быть пустой
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Передан пустой почтовый адрес");
        }

        // Почта должна соответствовать формату e-mail
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(user.getEmail());

        if (!matcher.matches()) {
            throw new ValidationException("Передан неправильный почтовый адрес: " + user.getEmail());
        }

        // Почта не должна быть ранее использована
        boolean exists = userStorage.isMailAlreadyUsed(user);

        if (exists) {
            throw new ValidationException("Электронная почта " + user.getEmail() + " уже используется");
        }
        log.debug("Передано корректное значение email: {}", user.getEmail());
        log.debug("Валидация электронной успешно почты завершена");
    }

    /**
     * Валидация логина сущности {@link User}
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateLogin(User user) throws ValidationException {
        log.debug("Запускаем валидацию логина");
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
        log.debug("Передано корректное значение login: {}", user.getLogin());
        log.debug("Валидация логина успешно завершена");
    }

    /**
     * Валидация даты рождения сущности {@link User}
     *
     * @param user экземпляр класса {@link User}
     */
    private void validateBirthday(User user) {
        log.debug("Запускаем валидацию даты рождения");
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения должна быть меньше текущей даты");
        }
        log.debug("Передано корректное значение birthday: {}", user.getBirthday().format(DATE_FORMATTER));
        log.debug("Валидация даты рождения успешно завершена");
    }
}
