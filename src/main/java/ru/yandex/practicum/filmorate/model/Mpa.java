package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Рейтинг
 */
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"id", "name"})
@NoArgsConstructor
public class Mpa {

    /**
     * Идентификатор сущности
     */
    private Long id;

    /**
     * Наименование
     */
    private String name;
}
