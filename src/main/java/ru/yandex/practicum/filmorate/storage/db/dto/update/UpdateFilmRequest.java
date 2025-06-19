package ru.yandex.practicum.filmorate.storage.db.dto.update;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.storage.db.dto.read.GenreDto;
import ru.yandex.practicum.filmorate.storage.db.dto.read.MpaDto;
import ru.yandex.practicum.filmorate.storage.db.dto.read.UserDto;

@Builder
@Data
public class UpdateFilmRequest {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;
    private Set<UserDto> likes;

    public boolean hasId() {
        return id != null && id > 0;
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null;
    }

    public boolean hasMpa() {
        return mpa != null;
    }

    public boolean hasGenres() {
        return !(genres == null || genres.isEmpty());
    }

    public boolean hasLikes() {
        return !(likes == null || likes.isEmpty());
    }
}
