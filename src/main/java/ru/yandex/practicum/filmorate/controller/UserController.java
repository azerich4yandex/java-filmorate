package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

/**
 * Контроллер для обработки HTTP-запросов для "/users"
 */
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    /**
     * Обработка GET-запроса на /users
     *
     * @return коллекция сохранённых {@link  UserDto}
     */
    @GetMapping
    public ResponseEntity<Collection<UserDto>> findAll(@RequestParam(name = "size", defaultValue = "10") Integer size,
                                                       @RequestParam(name = "from", defaultValue = "0") Integer from) {
        log.info("Запрос всех пользователей на уровне контроллера");
        log.debug("Размер коллекции: {}", size);
        log.debug("Стартовый номер элемента: {}", from);

        Collection<UserDto> result = userService.findAll(size, from);
        log.debug("На уровень контроллера вернулась коллекция размером {} записей", result.size());

        log.info("Возврат результатов на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /users/{id}/friends
     *
     * @param id идентификатор пользователя
     * @return коллекция идентификаторов друзей пользователя
     */
    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<UserDto>> findFriends(@PathVariable Long id) {
        log.info("Поиск друзей пользователя на уровне контроллера");
        log.debug("Передан id пользователя: {}", id);

        Collection<UserDto> result = userService.findFriends(id);
        log.debug("На уровень контроллера вернулась коллекция друзей размером {}", result.size());

        log.info("Возврат результатов поиска друзей на уровень клиента");
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
    public ResponseEntity<Collection<UserDto>> findCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Поиск друзей общих друзей двух пользователей на уровне контроллера");
        log.debug("Передан id первого пользователя: {}", id);
        log.debug("Передан id второго пользователя: {}", otherId);

        Collection<UserDto> result = userService.findCommonFriends(id, otherId);
        log.debug("На уровень контроллера вернулась коллекция общих друзей размером {}", result.size());

        log.info("Возврат результата поиска общих друзей на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /users/{id}/recommendations
     *
     * @param userId идентификатор пользователя
     * @return коллекция фильмов для просмотра
     */
    @GetMapping("/{id}/recommendations")
    public ResponseEntity<Collection<FilmDto>> getUserRecommendations(@PathVariable(name = "id") Long userId) {
        log.info("Поиск рекомендаций для пользователя");
        log.debug("Передан  id пользователя: {}", userId);
        Collection<FilmDto> result = userService.getUserRecommendations(userId);
        log.debug("На уровень контроллера вернулась коллекция рекомендаций размером {}", result.size());

        log.info("Возврат результатов поиска рекомендаций на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /users/{id}
     *
     * @param id идентификатор {@link User}
     * @return экземпляр класса {@link UserDto}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        log.info("Поиск пользователя по id на уровне контроллера");
        log.debug("Передан id: {}", id);

        UserDto user = userService.findById(id);
        log.debug("На уровень контроллера вернулся пользователь с id {}", user.getId());

        log.info("Возврат результата на уровень клиента");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Обработка POST-запроса для /users
     *
     * @param request сущность {@link User} из тела запроса
     * @return {@link User} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody NewUserRequest request) {
        log.info("Запрошено создание пользователя на уровне контроллера");

        UserDto result = userService.create(request);
        log.debug("На уровень контроллера после добавления вернулся пользователь с id {}", result.getId());

        log.info("Возврат результата создания на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса для /users
     *
     * @param newUser несохранённый пользователь с изменениями
     * @return пользователь с сохранёнными изменениями
     */
    @PutMapping
    public ResponseEntity<UserDto> update(@RequestBody UpdateUserRequest newUser) {
        log.info("Запрошено изменение пользователя на уровне контроллера");

        UserDto updatedUser = userService.update(newUser);
        log.debug("На уровень контроллера после изменения вернулся пользователь с id {}", updatedUser.getId());

        log.info("Возврат результата обновления на уровень клиента");
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    /**
     * Обработка PUT-запроса для /users/{id}/friends/{friendId}
     *
     * @param id идентификатор пользователя
     * @param friendId идентификатор друга
     * @return статус успешности операции
     */
    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Запрошено добавление в друзья на уровне контроллера");
        log.debug("Передан id основного пользователя: {}", id);
        log.debug("Передан id друга: {}", friendId);

        userService.addFriend(id, friendId);

        log.info("Возврат результата добавления на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /users/{userId}/friends/{friendId}
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable(name = "userId") Long userId,
                                             @PathVariable(name = "friendId") Long friendId) {
        log.info("Запрошено удаление из друзей");
        log.debug("Передан userId  пользователя: {}", userId);
        log.debug("Передан userId  друга: {}", friendId);

        userService.removeFriend(userId, friendId);

        log.info("Возврат результата удаления друга на уровень клиента");
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
        log.info("Передан id удаляемого пользователя: {}", id);

        userService.deleteUser(id);

        log.info("Возврат результата удаления пользователя на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /users
     */
    @DeleteMapping
    public ResponseEntity<Void> clearUsers() {
        log.info("Запрошена очистка списка фильмов на уровне контроллера");

        userService.clearUsers();

        log.info("Возврат результатов очистки списка пользователей на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
