package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/***
 * User.
 */
@Builder
@Data
public class User {

    /***
     * Идентификатор пользователя
     */
    private Long id;

    /***
     * Электронная почта пользователя
     */
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Переданное значение не является адресом электронной почты")
    private String email;

    /***
     * Логин пользователя
     */
    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    /***
     * Отображаемое имя пользователя
     */
    @Getter(AccessLevel.NONE)
    private String name;

    /***
     * Дата рождения пользователя
     */
    @PastOrPresent
    private LocalDate birthday;

    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        } else {
            return name;
        }
    }

    @AssertTrue(message = "Логин не должен содержать пробелы.")
    public boolean isValidLogin() {
        return login != null && !login.contains(" ");
    }
}
