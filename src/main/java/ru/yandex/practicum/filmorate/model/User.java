package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/***
 * User.
 */
@Data
public class User {

    /***
     * Идентификатор пользователя
     */
    private Long id;

    /***
     * Электронная почта пользователя
     */
    private String email;

    /***
     * Логин пользователя
     */
    private String login;

    /***
     * Отображаемое имя пользователя
     */
    @Getter(AccessLevel.NONE)
    private String name;

    /***
     * Дата рождения пользователя
     */
    private LocalDate birthday;

    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        } else {
            return name;
        }
    }
}
