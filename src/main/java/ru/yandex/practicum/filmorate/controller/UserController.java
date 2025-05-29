package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

/**
 * Контроллер для обработки HTTP-запросов для "/users"
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Обработка GET-запроса на /users
     *
     * @return коллекция сохранённых {@link  User}
     */
    @GetMapping
    public ResponseEntity<Collection<User>> findAll() {
        log.info("Запрос всех пользователей на уровне контроллера");

        Collection<User> result = userService.findAll();
        log.debug("Получена коллекция размером {} записей", result.size());

        log.info("Возврат результатов на уровень пользователя");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /users/{id}
     *
     * @param id идентификатор {@link User}
     * @return экземпляр класса {@link User} или null
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        log.info("Поиск пользователя по id на уровне контроллера");
        log.debug("Передан id: {}", id);

        Optional<User> userOpt = userService.findById(id);

        User user = null;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        }

        log.info("Возврат результата на уровень пользователя");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /users/{id}/friends
     *
     * @param id идентификатор пользователя
     * @return коллекция друзей пользователя
     */
    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<User>> findFriends(@PathVariable Long id) {
        log.info("Поиск друзей пользователя на уровне контроллера");
        log.debug("Передан id пользователя: {}", id);

        Collection<User> result = userService.findFriends(id);

        log.info("Возврат результатов поиска друзей на уровень пользователя");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /users/{id}/common/{otherId}
     *
     * @param id идентификатор первого пользователя
     * @param otherId идентификатор второго пользователя
     * @return коллекция общих друзей
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> findCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Поиск друзей общих друзей двух пользователей на уровне контроллера");
        log.debug("Передан id первого пользователя: {}", id);
        log.debug("Передан id второго пользователя: {}", otherId);

        Collection<User> result = userService.findCommonFriends(id, otherId);

        log.info("Возврат результата поиска общих друзей на уровень пользователя");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка POST-запроса для /users
     *
     * @param user сущность {@link User} из тела запроса
     * @return {@link User} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        log.info("Запрошено создание пользователя на уровне контроллера");

        user = userService.create(user);

        log.info("Возврат результата создания на уровень пользователя");
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса для /users
     *
     * @param newUser сущность {@link User} из тела запроса
     * @return {@link User} с изменёнными значениями
     */
    @PutMapping
    public ResponseEntity<User> update(@RequestBody User newUser) {
        log.info("Запрошено изменение пользователя");

        User existingUser = userService.update(newUser);

        log.info("Возврат результата обновления на уровень пользователя");
        return new ResponseEntity<>(existingUser, HttpStatus.OK);
    }

    /**
     * Обработка PUT-запроса для /users/{id}/friends/{friendId}
     *
     * @param id идентификатор пользователя
     * @param friendId идентификатор друга
     * @return статус успешности операции
     */
    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрошено добавление в друзья");
        log.debug("Передан id основного пользователя: {}", id);
        log.debug("Передан id друга: {}", friendId);

        User friend = userService.addFriend(id, friendId);

        log.info("Возврат результата добавления на уровень пользователя");
        return new ResponseEntity<>(friend, HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /users/{id}/friends/{friendId}
     *
     * @param id идентификатор пользователя
     * @param friendId идентификатор друга
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрошено удаление из друзей");
        log.debug("Передан id  пользователя: {}", id);
        log.debug("Передан id  друга: {}", friendId);

        userService.removeFriend(id, friendId);

        log.info("Возврат результата удаления на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запрос для /users/{id}
     *
     * @param id идентификатор пользователя
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Запрошено удаление пользователя на уровне контроллера");

        userService.deleteUser(id);

        log.info("Пользователь удалён");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /users
     */
    @DeleteMapping
    public ResponseEntity<Void> clearUsers() {
        log.info("Запрошена очистка списка фильмов на уровне контроллера");

        userService.clearUsers();

        log.info("Возврат результатов очистки списка пользователей");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
