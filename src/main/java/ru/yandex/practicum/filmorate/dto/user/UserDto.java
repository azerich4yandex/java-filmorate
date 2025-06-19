package ru.yandex.practicum.filmorate.dto.user;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {

    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<UserShortDto> friends;
}
