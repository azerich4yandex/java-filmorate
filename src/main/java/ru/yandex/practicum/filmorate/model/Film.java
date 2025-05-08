package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    /***
     * Описание
     */
    @NotBlank(message = "Описание должно быть указано")
    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    /***
     * Дата релиза
     */
    private LocalDate releaseDate;

    /***
     * Длительность
     */
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    @AssertTrue(message = "Дата релиза - не раньше 28 декабря 1895 года")
    public boolean isValidReleaseDate() {
        if (releaseDate != null) {
            return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
        } else {
            return true;
        }
    }
}
