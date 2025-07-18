package ru.yandex.practicum.filmorate.dto.user;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NewUserRequest {

    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
