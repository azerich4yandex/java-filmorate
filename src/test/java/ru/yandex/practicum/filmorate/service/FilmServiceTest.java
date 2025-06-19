package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Работа с хранилищем фильмов")
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class FilmServiceTest {

    private final FilmService filmService;

    private final Film film1 = Film.builder()
            .name("Film name")
            .description("Film description")
            .releaseDate(LocalDate.now().minusYears(5))
            .duration(120)
            .build();

    private final Film film2 = Film.builder()
            .name("Film2 name")
            .description("Film2 description")
            .releaseDate(LocalDate.now().minusYears(4))
            .duration(110)
            .build();

    @DisplayName("Создание фильма")
    @Test
    public void createFilmTest() {
        FilmDto addedDto = filmService.create(FilmMapper.mapToNewFilmRequest(film1));
        assertNotNull(addedDto);
        assertNotNull(addedDto.getId());
        assertEquals(film1.getName(), addedDto.getName());
        assertEquals(film1.getDescription(), addedDto.getDescription());
    }

    @DisplayName("Получение фильма по идентификатору")
    @Test
    public void getFilmByIdTest() {
        filmService.create(FilmMapper.mapToNewFilmRequest(film1));
        FilmDto dbFilm = filmService.findById(1L);
        assertThat(dbFilm).hasFieldOrPropertyWithValue("id", 1L);
    }

    @DisplayName("Обновление фильма")
    @Test
    public void updateFilmTest() {
        FilmDto createdDto = filmService.create(FilmMapper.mapToNewFilmRequest(film1));
        Film created = FilmMapper.mapToFilm(createdDto);
        assertNotNull(created);
        assertNotNull(created.getId());

        created.setName("Film updated");
        filmService.update(FilmMapper.mapToUpdateFilmRequest(created));
        FilmDto dbFilm = filmService.findById(created.getId());
        assertThat(dbFilm).hasFieldOrPropertyWithValue("name", "Film updated");
    }

    @DisplayName("Удаление фильма")
    @Test
    public void deleteFilmTest() {
        FilmDto addedFilm = filmService.create(FilmMapper.mapToNewFilmRequest(film2));
        Collection<FilmDto> beforeDelete = filmService.findAll(10, 0);
        filmService.deleteFilm(addedFilm.getId());
        Collection<FilmDto> afterDelete = filmService.findAll(10, 0);
        assertEquals(beforeDelete.size() - 1, afterDelete.size());
    }
}