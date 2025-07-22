package ru.yandex.practicum.filmorate.dto.film;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;

@Builder
@Data
public class NewFilmRequest {

    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;
    private Set<DirectorDto> directors;
}
