package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FilmorateApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private UserController userController;

    @Autowired
    private FilmController filmController;

    @DisplayName("Контроллеры должны создаться")
    @Test
    void contextLoads() {
        // Проверяем, что контроллеры существуют
        assertNotNull(userController);
        assertNotNull(filmController);
    }
}
