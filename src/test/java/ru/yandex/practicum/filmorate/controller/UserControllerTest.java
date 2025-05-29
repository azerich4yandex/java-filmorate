package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
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
        user1 = User.builder().name("a").email("a@mail.ru").login("A").birthday(LocalDate.now().minusYears(20)).build();

        user2 = User.builder().name("b").email("b@mail.ru").login("B").birthday(LocalDate.now().minusYears(20)).build();

        user3 = User.builder().name("c").email("c@mail.ru").login("C").birthday(LocalDate.now().minusYears(20)).build();
    }

    @AfterEach
    void halt() {
        // Очищаем хранилище
        userController.clearUsers();
    }

    @DisplayName("Операции с пользователями")
    @Test
    void userCreate() {
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

        // Получаем список пользователей после добавления
        responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.contains(user1));

        // Убираем имя пользователя
        String tempString = user1.getName();
        user1.setName(null);
        responseUser = userController.update(user1);
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());
        user1 = responseUser.getBody();
        assertNotNull(user1);
        assertEquals(user1.getLogin(), user1.getName());

        // Возвращаем имя пользователя
        user1.setName(tempString);
        responseUser = userController.update(user1);
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());
        user1 = responseUser.getBody();

        // Убираем адрес пользователя
        tempString = user1.getEmail();
        user1.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Ставим неправильный адрес пользователя
        user1.setEmail("@@.ru");
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Ставим повторяющийся адрес почты
        user1.setEmail(tempString);
        tempString = user2.getEmail();
        user2.setEmail(user1.getEmail());

        assertThrows(ValidationException.class, () -> userController.create(user2));
        user2.setEmail(tempString);

        // Убираем логин пользователя
        tempString = user1.getLogin();
        user1.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем неправильный логин
        user1.setLogin("a a");
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем повторяющийся логин
        user1.setLogin(tempString);
        tempString = user2.getLogin();
        user2.setLogin(user1.getLogin());
        assertThrows(ValidationException.class, () -> userController.create(user2));
        user2.setLogin(tempString);

        // Устанавливаем пустую дату рождения
        user1.setBirthday(null);
        assertThrows(ValidationException.class, () -> userController.update(user1));

        // Устанавливаем неправильную дату рождения
        user1.setBirthday(LocalDate.now().plusYears(1));
        assertThrows(ValidationException.class, () -> userController.update(user1));
        user1.setBirthday(LocalDate.now().minusYears(20));

        // Устанавливаем неизвестный id
        Long tempId = user1.getId();
        user1.setId(user1.getId() + 1);
        assertThrows(NotFoundException.class, () -> userController.update(user1));
        user1.setId(tempId);

        // Удаляем сохранённого пользователя
        ResponseEntity<Void> responseVoid = userController.deleteUser(user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Удаляем несохранённого пользователя
        assertThrows(NotFoundException.class, () -> userController.deleteUser(user1.getId() + 1));

        // Удаляем пользователя с id = null
        assertThrows(ValidationException.class, () -> userController.deleteUser(null));

        // Получаем список пользователей после удаления
        responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertTrue(users.isEmpty());

        // Добавляем всех пользователей
        user1.setId(null);
        user2.setId(null);
        user3.setId(null);
        responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user1 = responseUser.getBody();
        responseUser = userController.create(user2);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user2 = responseUser.getBody();
        responseUser = userController.create(user3);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user3 = responseUser.getBody();
        Collection<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(user1);
        expectedUsers.add(user2);
        expectedUsers.add(user3);

        // Получаем список всех пользователей после добавления
        responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(expectedUsers.size(), users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));

        // Получаем пользователя по известному id
        responseUser = userController.findById(user1.getId());
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());
        user1 = responseUser.getBody();
        assertNotNull(user1);
        assertNotNull(user1.getId());

        // Получаем пользователя по неизвестному id
        assertThrows(NotFoundException.class, () -> userController.findById(user3.getId() * 100));

        // Получаем пользователя по пустому id
        assertThrows(ValidationException.class, () -> userController.findById(null));

        // Очищаем хранилище от пользователей
        responseVoid = userController.clearUsers();
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());
        responseUsers = userController.findAll();
        assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        users = responseUsers.getBody();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @DisplayName("Добавление/удаление друзей")
    @Test
    void friendAdd() {
        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        user1 = responseUser.getBody();
        assertNotNull(user1);

        // Получаем первого пользователя по id
        responseUser = userController.findById(user1.getId());
        user1 = responseUser.getBody();
        assertNotNull(user1);

        // Добавляем второго пользователя
        responseUser = userController.create(user2);
        user2 = responseUser.getBody();
        assertNotNull(user2);

        // Получаем второго пользователя по id
        responseUser = userController.findById(user2.getId());
        user2 = responseUser.getBody();
        assertNotNull(user2);

        // Добавляем дружбу
        responseUser = userController.addFriend(user1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());

        // Получаем список друзей первого пользователя
        ResponseEntity<Collection<User>> responseFriends = userController.findFriends(user1.getId());
        assertEquals(HttpStatus.OK, responseFriends.getStatusCode());
        Collection<User> user1Friends = responseFriends.getBody();
        assertNotNull(user1Friends);
        assertFalse(user1Friends.isEmpty());
        assertTrue(user1Friends.contains(user2));

        // Получаем список друзей второго пользователя
        responseFriends = userController.findFriends(user2.getId());
        assertEquals(HttpStatus.OK, responseFriends.getStatusCode());
        Collection<User> user2Friends = responseFriends.getBody();
        assertNotNull(user2Friends);
        assertFalse(user2Friends.isEmpty());
        assertTrue(user2Friends.contains(user1));

        // Удаляем дружбу
        ResponseEntity<Void> responseVoid = userController.removeFriend(user2.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем список друзей первого пользователя после удаления
        responseFriends = userController.findFriends(user1.getId());
        assertEquals(HttpStatus.OK, responseFriends.getStatusCode());
        user1Friends = responseFriends.getBody();
        assertNotNull(user1Friends);
        assertTrue(user1Friends.isEmpty());

        // Получаем список друзей второго пользователя после удаления
        responseFriends = userController.findFriends(user2.getId());
        assertEquals(HttpStatus.OK, responseFriends.getStatusCode());
        user2Friends = responseFriends.getBody();
        assertNotNull(user2Friends);
        assertTrue(user2Friends.isEmpty());

        // Добавляем третьего пользователя
        responseUser = userController.create(user3);
        user3 = responseUser.getBody();
        assertNotNull(user3);

        // Добавляем дружбу первого и третьего
        responseUser = userController.addFriend(user1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());

        // Добавляем дружбу второго и третьего
        responseUser = userController.addFriend(user2.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());

        // Получаем список общих друзей между первым и вторым
        responseFriends = userController.findCommonFriends(user1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseFriends.getStatusCode());
        Collection<User> commonFriends = responseFriends.getBody();
        assertNotNull(commonFriends);
        assertFalse(commonFriends.isEmpty());
        User commonFriend = commonFriends.stream().findFirst().get();
        assertEquals(user3, commonFriend);

        // Удаляем дружбу второго и третьего
        responseVoid = userController.removeFriend(user2.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Повторно получаем список общих друзей между первым и вторым
        responseFriends = userController.findCommonFriends(user1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseFriends.getStatusCode());
        commonFriends = responseFriends.getBody();
        assertNotNull(commonFriends);
        assertTrue(commonFriends.isEmpty());

        // Удаляем дружбу между третьим и первым (в порядке, обратном добавлению)
        responseVoid = userController.removeFriend(user3.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Добавляем дружбу
        responseUser = userController.addFriend(user1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseUser.getStatusCode());

        // Удаляем второго пользователя
        responseVoid = userController.deleteUser(user2.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем удаленного пользователя
        assertThrows(NotFoundException.class, () -> userController.findById(user2.getId()));

        // Получаем друзей удаленного пользователя
        assertThrows(NotFoundException.class, () -> userController.findFriends(user2.getId()));

        // Получаем друзей первого пользователя
        responseFriends = userController.findFriends(user1.getId());
        assertEquals(HttpStatus.OK, responseFriends.getStatusCode());
        user1Friends = responseFriends.getBody();
        assertNotNull(user1Friends);
        assertTrue(user1Friends.isEmpty());
    }
}
