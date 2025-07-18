package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Review {

    /**
     * Идентификатор сущности
     */
    private Long id;

    /**
     * Содержимое отзыва
     */
    private String content;

    /**
     * Признак положительного образа
     */
    @Builder.Default
    private boolean isPositive = false;

    /**
     * Идентификатор пользователя-автора
     */
    private Long userId;

    /**
     * Идентификатор фильма, на который оставляют отзыв
     */
    private Long filmId;

    /**
     * Рейтинг полезности отзыва
     */
    @Builder.Default
    private Integer useful = 0;
}
