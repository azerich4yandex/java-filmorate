package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectorMapper {

    public static Director mapToDirector(NewDirectorRequest request) {
        return Director.builder()
                .name(request.getName())
                .build();
    }

    public static DirectorDto mapToDirectorDto(Director director) {
        return DirectorDto.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }

    public static Director updateDirectorFields(Director director, UpdateDirectorRequest request) {
        if (request.hasId()) {
            director.setId(request.getId());
        }

        if (request.hasName()) {
            director.setName(request.getName());
        }

        return director;
    }
}
