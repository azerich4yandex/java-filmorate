package ru.yandex.practicum.filmorate.service;

import java.util.Arrays;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("Работа с хранилищем жанров")
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class GenreServiceTest {

    private final GenreService genreService;

    @DisplayName("Получение списка жанров")
    @Test
    public void getAllGenresTest() {
        Collection<GenreDto> genre = genreService.findAll(10, 0);
        assertThat(genre).extracting(GenreDto::getName)
                .containsAll(Arrays.asList("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик"));
    }

    @DisplayName("Получение жанра по идентификатору")
    @Test
    public void getGenreByIdTest() {
        GenreDto genre = genreService.findById(4L);
        assertEquals("Триллер", genre.getName());
    }
}
