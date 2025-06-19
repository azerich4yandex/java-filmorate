package ru.yandex.practicum.filmorate.dto.genre;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NewGenreRequest {

    private String name;
}
