package ru.yandex.practicum.filmorate.dto.user;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateUserRequest {

    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<UserDto> friends;

    public boolean hasId() {
        return id != null && id > 0;
    }

    public boolean hasEmail() {
        return !(email == null || email.isBlank());
    }

    public boolean hasLogin() {
        return !(login == null || login.isBlank());
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasBirthday() {
        return birthday != null;
    }

    public boolean hasFriends() {
        return !(friends == null || friends.isEmpty());
    }
}
