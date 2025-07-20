package ru.yandex.practicum.filmorate.dto.director;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DirectorDto {

    private Long id;
    private String name;
}
