package ru.yandex.practicum.filmorate.storage.db.dto.read;

import java.time.LocalDate;
import java.util.TreeSet;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto implements Comparable<UserDto> {

    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private TreeSet<UserShortDto> friends;

    @Override
    public int compareTo(UserDto o) {
        if (this.id < o.getId()) {
            return -1;
        } else if (this.id > o.getId()) {
            return 1;
        } else {
            return 0;
        }
    }
}
