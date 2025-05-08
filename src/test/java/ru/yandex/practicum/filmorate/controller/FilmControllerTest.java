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
import ru.yandex.practicum.filmorate.model.Film;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FilmControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FilmController filmController;

    @AfterEach
    void halt() {
        filmController.findAll().clear();
    }

    @DisplayName("При отсутствии данных контроллер должен возвращать пустую коллекцию")
    @Test
    public void shouldBeReturnEmptyCollection() {
        List<Film> expectedFilms = new ArrayList<>();

        List<Film> receivedFilms = filmController.findAll().stream().toList();

        assertTrue(receivedFilms.isEmpty());
        assertEquals(expectedFilms, receivedFilms);
    }

    @DisplayName("Добавление корректного фильма")
    @Test
    void addingShouldGoThroughWithoutErrors() {
        Film expectedFilm = Film.builder().name("Name").description("Description").duration(120)
                .releaseDate(LocalDate.now()).build();

        Film receivedFilm = filmController.create(expectedFilm);

        assertNotNull(receivedFilm);
        assertNotNull(receivedFilm.getId());
        assertEquals(expectedFilm.getName(), receivedFilm.getName());
        assertEquals(expectedFilm.getDescription(), receivedFilm.getDescription());
        assertEquals(expectedFilm.getReleaseDate(), receivedFilm.getReleaseDate());
        assertEquals(expectedFilm.getDuration(), receivedFilm.getDuration());

        List<Film> receivedFilms = filmController.findAll().stream().toList();

        assertFalse(receivedFilms.isEmpty());
    }

    @DisplayName("Обновление корректного фильма")
    @Test
    void updatingShouldGoThroughWithoutErrors() {
        Film expectedFilm = Film.builder().name("Name").description("Description").releaseDate(LocalDate.now())
                .duration(120).build();

        Film receivedFilm = filmController.create(expectedFilm);

        assertNotNull(receivedFilm);
        assertNotNull(receivedFilm.getId());

        expectedFilm.setId(receivedFilm.getId());
        expectedFilm.setName("Updated name");
        expectedFilm.setDescription("Updated description");
        expectedFilm.setReleaseDate(LocalDate.now().minusDays(1));
        expectedFilm.setDuration(110);

        receivedFilm = filmController.update(receivedFilm);

        assertNotNull(receivedFilm);
        assertEquals(expectedFilm.getName(), receivedFilm.getName());
        assertEquals(expectedFilm.getDescription(), receivedFilm.getDescription());
        assertEquals(expectedFilm.getReleaseDate(), receivedFilm.getReleaseDate());
        assertEquals(expectedFilm.getDuration(), receivedFilm.getDuration());
    }
}
