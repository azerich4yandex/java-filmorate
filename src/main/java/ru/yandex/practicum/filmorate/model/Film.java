package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Фильм
 */
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Film {

    /**
     * Режиссеры фильма
     */
    private final Set<Long> directors = new HashSet<>();

    /**
     * Отзывы о фильме
     */
    private final Set<Long> reviews = new HashSet<>();

    /**
     * Список идентификаторов пользователей, которым понравился фильм
     */
    private final Set<Long> likes = new HashSet<>();

    /**
     * Список жанров фильма
     */
    private final Set<Long> genres = new HashSet<>();

    /**
     * Идентификатор сущности
     */
    private Long id;

    /**
     * Название
     */
    private String name;

    /**
     * Описание
     */
    private String description;

    /**
     * Дата релиза
     */
    private LocalDate releaseDate;

    /**
     * Длительность
     */
    private Integer duration;

    /**
     * Рейтинг
     */
    private Mpa mpa;

    /**
     * Метод обновляет коллекцию идентификаторов жанров фильма
     *
     * @param genreIds коллекция идентификаторов жанров фильма
     */
    public void setGenres(Collection<Long> genreIds) {
        this.genres.clear();
        this.genres.addAll(genreIds);

    }

    /**
     * Метод обновляет коллекцию идентификаторов режиссеров
     *
     * @param directorIds коллекция идентификаторов режиссеров
     */
    public void setDirectors(Collection<Long> directorIds) {
        this.directors.clear();
        this.directors.addAll(directorIds);
    }
}
