package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserController userController;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void init() {
        user1 = User.builder().name("a").email("a@mail.ru").login("A").birthday(LocalDate.now().minusYears(21)).build();
        user2 = User.builder().name("b").email("b@mail.ru").login("B").birthday(LocalDate.now().minusYears(22)).build();
        user3 = User.builder().name("c").email("c@mail.ru").login("C").birthday(LocalDate.now().minusYears(23)).build();
    }

    @AfterEach
    void halt() {
        // Очищаем хранилище
        userController.clearUsers();
    }

    @DisplayName("Добавление пользователя без вызова исключений")
    @Test
    void shouldCreateOrUpdateUserWithoutThrowingException() {
        // Получаем список пользователей до добавления
        ResponseEntity<Collection<User>> responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        Collection<User> users = responseUsers.getBody();
        assertNotNull(users);
        assertTrue(users.isEmpty());

        // Добавляем пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user1 = responseUser.getBody();
        assertNotNull(user1);
        assertNotNull(user1.getId());

        // Устанавливаем пустое имя
        user2.setName(null);
        responseUser = userController.create(user2);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user2 = responseUser.getBody();
        assertNotNull(user2);
        assertNotNull(user2.getId());
        assertEquals(user2.getLogin(), user2.getName());
    }

    @DisplayName("Получение пользователя без вызова исключений")
    @Test
    void shouldReturnUserWithoutThrowingExceptions() {
        // Добавляем пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user1 = responseUser.getBody();
        assertNotNull(user1);
        assertNotNull(user1.getId());

        responseUser = userController.findById(user1.getId());
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());
        User user = responseUser.getBody();
        assertNotNull(user);
        assertNotNull(user);
        assertEquals(user1, user);
    }

    @DisplayName("Вызов исключений при получении пользователя по id")
    @Test
    void shouldThrowNotFoundOrValidationExceptionWhenFindById() {
        // Получаем пользователя по отрицательному id
        assertThrows(NotFoundException.class, () -> userController.findById(-1L));

        // Получаем пользователя по неизвестному id
        assertThrows(NotFoundException.class, () -> userController.findById(5L));

        // Получаем пользователя по пустому id
        assertThrows(ValidationException.class, () -> userController.findById(null));
    }

    @DisplayName("Получение коллекции пользователей без вызова исключений")
    @Test
    void shouldReturnNotNullCollectionWhenFindAll() {
        // Получаем пустую коллекцию
        ResponseEntity<Collection<User>> responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        Collection<User> users = responseUsers.getBody();
        assertNotNull(users);
        assertTrue(users.isEmpty());

        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем второго пользователя
        responseUser = userController.create(user2);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем третьего пользователя
        responseUser = userController.create(user3);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Получаем список пользователей после добавления
        responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));
    }

    @DisplayName("Обновление пользователя с вызовом исключений")
    @Test
    void shouldThrowValidationOrNotFoundExceptionWhenUpdate() {
        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем второго пользователя
        responseUser = userController.create(user2);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Устанавливаем неизвестный id
        Long tempLong = user1.getId();
        user1.setId(tempLong * 10);
        assertThrows(NotFoundException.class, () -> userController.update(user1));
        user1.setId(tempLong);

        // Устанавливаем пустой логин
        String tempString = user1.getLogin();
        user1.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем логин с пробелами
        user1.setLogin(tempString + " " + tempString);
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем повторяющийся логин
        user1.setLogin(user2.getLogin());
        assertThrows(ValidationException.class, () -> userController.update(user1));
        user1.setLogin(tempString);

        // Устанавливаем пустую почту
        tempString = user1.getEmail();
        user1.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем неправильную почту
        user1.setEmail(" a@@.ru");
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем повторяющуюся почту
        user1.setEmail(user2.getEmail());
        assertThrows(ValidationException.class, () -> userController.update(user1));
        user1.setEmail(tempString);

        // Устанавливаем пустую дату рождения
        user1.setBirthday(null);
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем дату рождения из будущего
        user1.setBirthday(LocalDate.now().plusYears(1));
        assertThrows(ValidationException.class, () -> userController.update(user1));
    }

    @DisplayName("Корректное и некорректное удаление пользователя")
    @Test
    void shouldDeleteOrThrowValidationOrNotFoundExceptions() {
        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Удаляем фильм
        ResponseEntity<Void> responseVoid = userController.deleteUser(user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Повторно удаляем тот же фильм
        assertThrows(NotFoundException.class, () -> userController.deleteUser(user1.getId()));

        // Удаляем фильм с несуществующим id
        assertThrows(NotFoundException.class, () -> userController.deleteUser(user1.getId() * 100));

        // Удаляем фильм с отрицательным id
        assertThrows(NotFoundException.class, () -> userController.deleteUser(-1 * user1.getId()));

        // Удаляем фильм с null id
        assertThrows(ValidationException.class, () -> userController.deleteUser(null));

    }

    @DisplayName("Очистка хранилища пользователей без вызова исключений")
    @Test
    void shouldClearStorageWithoutThrowingException() {
        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем второго пользователя
        responseUser = userController.create(user2);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем третьего пользователя
        responseUser = userController.create(user3);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Получаем список пользователей из хранилища
        ResponseEntity<Collection<User>> responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        Collection<User> users = responseUsers.getBody();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));

        // Очищаем хранилище
        ResponseEntity<Void> responseVoid = userController.clearUsers();
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем список пользователей из хранилища после удаления
        responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @DisplayName("Добавление/удаление друзей  и получение общих друзей без вызова исключений")
    @Test
    void shouldAddOrRemoveFriendsWithoutThrowingException() {
        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем второго пользователя
        responseUser = userController.create(user2);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем третьего пользователя
        responseUser = userController.create(user3);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем дружбу между первым и вторым пользователями
        ResponseEntity<Void> responseVoid = userController.addFriend(user1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Добавляем дружбу между вторым и третьим пользователями
        responseVoid = userController.addFriend(user2.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем список общих друзей между первым и третьим пользователями
        ResponseEntity<Collection<User>> responseUsers = userController.findCommonFriends(user1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        Collection<User> users = responseUsers.getBody();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertFalse(users.contains(user1));
        assertTrue(users.contains(user2));
        assertFalse(users.contains(user3));

        // Удаляем дружбу между первым и вторым пользователями
        responseVoid = userController.removeFriend(user1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем список общих друзей между первым и третьим пользователями
        responseUsers = userController.findCommonFriends(user1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertTrue(users.isEmpty());

        // Добавляем дружбу между первым и вторым пользователями
        responseVoid = userController.addFriend(user1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Добавляем дружбу между первым и третьим пользователями
        responseVoid = userController.addFriend(user1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем список друзей первого пользователя
        responseUsers = userController.findFriends(user1.getId());
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertFalse(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));

        // Удаляем третьего пользователя
        responseVoid = userController.deleteUser(user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем список друзей первого пользователя
        responseUsers = userController.findFriends(user1.getId());
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertFalse(users.contains(user1));
        assertTrue(users.contains(user2));
        assertFalse(users.contains(user3));
    }

    @DisplayName("Добавление/удаление друзей с вызовом исключений")
    @Test
    void shouldThrowNotFoundOrValidationExceptionWhenAddOrRemoveFriend() {
        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем третьего пользователя
        responseUser = userController.create(user3);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());

        // Добавляем дружбу первого пользователя с неизвестным
        assertThrows(NotFoundException.class, () -> userController.addFriend(user1.getId(), user3.getId() * 100));

        // Добавляем дружбу первого пользователя с отрицательным
        assertThrows(NotFoundException.class, () -> userController.addFriend(user1.getId(), -1 * user3.getId()));

        // Добавляем дружбу первого пользователя с null
        assertThrows(ValidationException.class, () -> userController.addFriend(user1.getId(), null));

        // Добавляем дружбу отрицательного пользователя с первым
        assertThrows(NotFoundException.class, () -> userController.addFriend(-1 * user3.getId(), user1.getId()));

        // Добавляем дружбу неизвестного пользователя с первым
        assertThrows(NotFoundException.class, () -> userController.addFriend(100 * user3.getId(), user1.getId()));

        // Добавляем дружбу null с первым
        assertThrows(ValidationException.class, () -> userController.addFriend(null, user1.getId()));

        // Добавляем дружбу первого и третьего пользователей
        ResponseEntity<Void> responseVoid = userController.addFriend(user1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Удаляем дружбу первого и третьего пользователей
        responseVoid = userController.removeFriend(user1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Повторно удаляем дружбу первого и третьего пользователей
        responseVoid = userController.removeFriend(user1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Удаляем дружбу отрицательного пользователя и третьего
        assertThrows(NotFoundException.class, () -> userController.removeFriend(-1 * user3.getId(), user1.getId()));

        // Удаляем дружбу неизвестного пользователя и третьего
        assertThrows(NotFoundException.class, () -> userController.removeFriend(100 * user3.getId(), user1.getId()));

        // Удаляем дружбу null и третьего
        assertThrows(ValidationException.class, () -> userController.removeFriend(null, user1.getId()));
    }
}
