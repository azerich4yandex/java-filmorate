package ru.yandex.practicum.filmorate.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = Film.builder()
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .build();

        if (request.getMpa() != null) {
            film.setMpa(MpaMapper.mapToMpa(request.getMpa()));
        }

        Collection<Long> genres = new ArrayList<>();
        if (!(request.getGenres() == null || request.getGenres().isEmpty())) {
            genres.addAll(request.getGenres().stream().map(GenreDto::getId).toList());
        }
        film.setGenres(genres);

        Collection<Long> directors = new ArrayList<>();
        if (!(request.getDirectors() == null || request.getDirectors().isEmpty())) {
            directors.addAll(request.getDirectors().stream().map(DirectorDto::getId).toList());
        }
        film.setDirectors(directors);

        return film;
    }

    public static Film mapToFilm(FilmDto dto) {
        Film film = Film.builder()
                .id(dto.getId())
                .name(dto.getName().trim())
                .description(dto.getDescription().trim())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .build();

        if (dto.getMpa() != null) {
            film.setMpa(MpaMapper.mapToMpa(dto.getMpa()));
        }

        Collection<Long> genres = new ArrayList<>();
        if (!(dto.getGenres() == null || dto.getGenres().isEmpty())) {
            genres.addAll(dto.getGenres().stream().map(GenreDto::getId).toList());
        }
        film.setGenres(genres);

        Collection<Long> directors = new ArrayList<>();
        if (!(dto.getDirectors() == null || dto.getDirectors().isEmpty())) {
            directors.addAll(dto.getDirectors().stream().map(DirectorDto::getId).toList());
        }
        film.setDirectors(directors);

        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription().trim())
                .duration(film.getDuration())
                .releaseDate(film.getReleaseDate())
                .build();

        if (film.getMpa() != null) {
            dto.setMpa(MpaMapper.mapToMpaDto(film.getMpa()));
        }

        return dto;
    }

    public static NewFilmRequest mapToNewFilmRequest(Film film) {
        NewFilmRequest request = NewFilmRequest.builder()
                .name(film.getName().trim())
                .description(film.getDescription().trim())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .build();

        if (film.getMpa() != null) {
            request.setMpa(MpaMapper.mapToMpaDto(film.getMpa()));
        }

        Set<GenreDto> genres = new HashSet<>();
        if (!(film.getGenres().isEmpty())) {
            for (Long genreId : film.getGenres()) {
                genres.add(GenreMapper.mapToGenreDto(Genre.builder().id(genreId).build()));
            }
        }
        request.setGenres(genres);

        Set<DirectorDto> directors = new HashSet<>();
        if (!(film.getDirectors().isEmpty())) {
            for (Long directorId : film.getDirectors()) {
                directors.add(DirectorMapper.mapToDirectorDto(Director.builder().id(directorId).build()));
            }
        }
        request.setDirectors(directors);

        return request;
    }

    public static UpdateFilmRequest mapToUpdateFilmRequest(Film film) {
        UpdateFilmRequest request = UpdateFilmRequest.builder()
                .id(film.getId())
                .name(film.getName().trim())
                .description(film.getDescription().trim())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .build();

        if (film.getMpa() != null) {
            request.setMpa(MpaMapper.mapToMpaDto(film.getMpa()));
        }

        Set<GenreDto> genres = new HashSet<>();
        if (!(film.getGenres().isEmpty())) {
            for (Long genreId : film.getGenres()) {
                genres.add(GenreMapper.mapToGenreDto(Genre.builder().id(genreId).build()));
            }
        }
        request.setGenres(genres);

        Set<UserDto> likes = new HashSet<>();
        if (!(film.getLikes().isEmpty())) {
            for (Long userId : film.getLikes()) {
                likes.add(UserMapper.mapToUserDto(User.builder().id(userId).build()));
            }
        }
        request.setLikes(likes);

        Set<DirectorDto> directors = new HashSet<>();
        if (!(film.getDirectors().isEmpty())) {
            for (Long directorId : film.getDirectors()) {
                directors.add(DirectorMapper.mapToDirectorDto(Director.builder().id(directorId).build()));
            }
        }
        request.setDirectors(directors);

        return request;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.hasId()) {
            film.setId(request.getId());
        }
        if (request.hasName()) {
            film.setName(request.getName().trim());
        }
        if (request.hasDescription()) {
            film.setDescription(request.getDescription().trim());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        if (request.hasMpa()) {
            film.setMpa(MpaMapper.mapToMpa(request.getMpa()));
        }
        if (request.hasGenres()) {
            film.setGenres(request.getGenres().stream().map(GenreDto::getId).toList());
        }

        if (request.hasDirectors()) {
            film.setDirectors(request.getDirectors().stream().map(DirectorDto::getId).toList());
        }

        return film;
    }
}
