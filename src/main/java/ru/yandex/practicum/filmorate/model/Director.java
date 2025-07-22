package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Режиссер
 */
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Director {

    /**
     * Идентификатор сущности
     */
    private Long id;

    /**
     * Имя режиссера
     */
    private String name;
}
