package ru.yandex.practicum.filmorate.storage.db.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.dto.create.NewGenreRequest;
import ru.yandex.practicum.filmorate.storage.db.dto.read.GenreDto;
import ru.yandex.practicum.filmorate.storage.db.dto.update.UpdateGenreRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenreMapper {

    public static Genre mapToGenre(NewGenreRequest request) {
        return Genre.builder()
                .name(request.getName().trim())
                .build();
    }

    public static Genre mapToGenre(GenreDto dto) {
        return Genre.builder()
                .id(dto.getId())
                .name(dto.getName().trim())
                .build();
    }

    public static GenreDto mapToGenreDto(Genre genre) {
        return GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public static NewGenreRequest mapToNewGenreRequest(Genre genre) {
        return NewGenreRequest.builder()
                .name(genre.getName())
                .build();
    }

    public static Genre updateGenreFields(Genre genre, UpdateGenreRequest request) {
        if (request.hasId()) {
            genre.setId(request.getId());
        }
        if (request.hasName()) {
            genre.setName(request.getName());
        }

        return genre;
    }
}
