package ru.yandex.practicum.filmorate.storage.db.dto.read;

import java.time.LocalDate;
import java.util.TreeSet;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FilmDto implements Comparable<FilmDto> {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private TreeSet<GenreDto> genres;
    private TreeSet<UserShortDto> likes;

    @Override
    public int compareTo(FilmDto o) {
        if (this.id < o.getId()) {
            return -1;
        } else if (this.id > o.getId()) {
            return 1;
        } else {
            return 0;
        }
    }
}
