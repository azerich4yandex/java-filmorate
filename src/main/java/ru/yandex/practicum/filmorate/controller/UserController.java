package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

/***
 * Контроллер для обработки методов GET, POST и PUT для "/users"
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final HashMap<Long, User> users = new HashMap<>();
    private long generatedId;

    /***
     * Обработка метода GET
     * @return коллекция сохранённых {@link  User}
     */
    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрошен список пользователей");
        Collection<User> result = users.values();
        log.debug(result.toString());
        return result;
    }

    /***
     * Обработка метода POST
     * @param user сущность {@link User} из тела запроса
     * @return {@link User} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Запрошено добавление пользователя");
        log.debug(user.toString());

        // Проводим валидацию
        validate(user);

        // Устанавливаем id
        user.setId(getNextId());
        log.info("Пользователь получил id");
        log.debug(user.toString());

        // Сохраняем в хранилище
        users.put(user.getId(), user);
        log.info("Пользователь добавлен в хранилище");

        // Возвращаем результат
        return user;
    }

    /***
     * Обработка метода PUT
     * @param newUser сущность {@link User} из тела запроса
     * @return {@link User} с изменёнными значениями
     */
    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Запрошено изменение пользователя");
        log.debug(newUser.toString());

        // Проверяем наличие id
        if (newUser.getId() == null) {
            log.warn("При обновлении передан пустой id");
            throw new ValidationException("Id должен быть указан");
        }

        // Ищем User в хранилище по id
        Optional<User> existingUserOpt = Optional.ofNullable(users.get(newUser.getId()));

        // Проверяем успешность поиска по id
        if (existingUserOpt.isEmpty()) {
            log.warn("Пользователь с id {} не найден в хранилище", newUser.getId());
            throw new ValidationException("Пользователь с id: " + newUser.getId() + " не найден");
        }

        User existingUser = existingUserOpt.get();
        boolean valuesAreChanged = false;

        // Проверяем изменение электронной почты
        if (!newUser.getEmail().equals(existingUser.getEmail())) {
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
            // Проводим валидацию
            log.debug("Начинаем валидацию данных");
            validate(existingUser);
            log.debug("Валидация прошла успешно");

            // Сохраняем изменения
            log.debug("Сохраняем изменения");
            users.put(existingUser.getId(), existingUser);
            log.info("Изменения сохранены");
        } else {
            log.info("Изменения не обнаружены");
        }

        // Возвращаем результат
        return existingUser;
    }

    /***
     * Генерация id для {@link User}
     * @return Следующее значение для id
     */
    private Long getNextId() {
        // Получаем новый id
        return ++generatedId;
    }


    /***
     * Валидация сущности {@link User} на заполнение ключевых полей.
     * Вызывает исключение {@link ValidationException} при нарушении правил.
     * @param user экземпляр сущности {@link User}
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
    }

    /***
     * Валидация электронной почты сущности {@link User}.
     * Вызывает исключение {@link ValidationException}
     * @param user экземпляр сущности {@link User}
     */
    private void validateEmail(User user) {
        // Почта не должна быть ранее использована
        boolean exists = users.values().stream().anyMatch(
                existingUser -> existingUser.getEmail().equals(user.getEmail()) && !existingUser.getId()
                        .equals(user.getId()));

        if (exists) {
            log.warn("Электронная почта {} уже используется другим пользователем", user.getEmail());
            throw new ValidationException("Электронная почта уже используется");
        }
    }

    /***
     * Валидация логина сущности {@link User}.
     * Вызывает исключение {@link ValidationException}
     * @param user экземпляр сущности {@link User}
     */
    private void validateLogin(User user) {
        // Логин не должен быть ранее использован
        boolean exists = users.values().stream().anyMatch(
                existingUser -> existingUser.getLogin().equals(user.getLogin()) && !existingUser.getId()
                        .equals(user.getId()));

        if (exists) {
            log.warn("Логин {} уже используется другим пользователем", user.getLogin());
            throw new ValidationException("Логин уде используется");
        }
    }
}
