package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserController userController;

    @AfterEach
    void halt() {
        userController.findAll().clear();
    }

    @DisplayName("При отсутствии данных контроллер должен возвращать пустую коллекцию")
    @Test
    public void shouldBeReturnEmptyCollection() {
        List<User> expectedUsers = new ArrayList<>();

        List<User> receivedUsers = userController.findAll().stream().toList();

        assertTrue(receivedUsers.isEmpty());
        assertEquals(expectedUsers, receivedUsers);
    }

    @DisplayName("Добавление корректного пользователя")
    @Test
    void addingShouldGoThroughWithoutErrors() {
        User expectedUser = User.builder()
                .email("b@b.ru")
                .login("a")
                .birthday(LocalDate.now().minusYears(20))
                .build();

        assertEquals(expectedUser.getName(), expectedUser.getLogin());

        User receivedUser = userController.create(expectedUser);

        assertNotNull(receivedUser);
        assertNotNull(receivedUser.getId());
        assertEquals(receivedUser.getName(), receivedUser.getLogin());

        assertEquals(expectedUser.getName(), receivedUser.getName());
        assertEquals(expectedUser.getEmail(), receivedUser.getEmail());
        assertEquals(expectedUser.getLogin(), receivedUser.getLogin());
        assertEquals(expectedUser.getBirthday(), receivedUser.getBirthday());

        List<User> receivedUsers = userController.findAll().stream().toList();

        assertFalse(receivedUsers.isEmpty());
    }

    @DisplayName("Обновление корректного фильма")
    @Test
    void updatingShouldGoThroughWithoutErrors() {
        User expectedUser = User.builder()
                .email("a@b.ru")
                .login("a")
                .birthday(LocalDate.now().minusYears(20))
                .build();

        User receivedUser = userController.create(expectedUser);

        assertNotNull(receivedUser);
        assertNotNull(receivedUser.getId());

        expectedUser.setId(receivedUser.getId());
        expectedUser.setName("A");
        expectedUser.setLogin("aa");
        expectedUser.setBirthday(expectedUser.getBirthday().minusYears(1));

        assertNotEquals(expectedUser.getLogin(), expectedUser.getName());

        receivedUser = userController.update(expectedUser);

        assertNotNull(receivedUser);
        assertEquals(expectedUser.getId(), receivedUser.getId());
        assertEquals(expectedUser.getName(), receivedUser.getName());
        assertEquals(expectedUser.getLogin(), receivedUser.getLogin());
        assertEquals(expectedUser.getBirthday(), receivedUser.getBirthday());
    }
}
