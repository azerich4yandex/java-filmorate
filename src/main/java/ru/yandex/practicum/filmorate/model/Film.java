package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Фильм
 */
@Builder
@Data
@EqualsAndHashCode(of = "id")
public class Film {

    /**
     * Список идентификаторов пользователей, которым понравился фильм
     */
    private final Set<Long> likes = new HashSet<>();

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
}
