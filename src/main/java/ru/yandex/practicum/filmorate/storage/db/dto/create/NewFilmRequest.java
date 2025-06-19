package ru.yandex.practicum.filmorate.storage.db.dto.create;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.storage.db.dto.read.GenreDto;
import ru.yandex.practicum.filmorate.storage.db.dto.read.MpaDto;

@Builder
@Data
public class NewFilmRequest {

    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;
}
