package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import lombok.Data;

/**
 * Film.
 */
@Data
public class Film {

    /***
     * Идентификатор сущности
     */
    private Long id;

    /***
     * Название
     */
    private String name;

    /***
     * Описание
     */
    private String description;

    /***
     * Дата релиза
     */
    private LocalDate releaseDate;

    /***
     * Длительность
     */
    private Integer duration;
}
