package ru.yandex.practicum.filmorate.dto.user;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserShortDto {

    private Long id;
    private String name;
    private Integer mark;
}
