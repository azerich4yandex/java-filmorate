package ru.yandex.practicum.filmorate.storage.db.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.dto.create.NewMpaRequest;
import ru.yandex.practicum.filmorate.storage.db.dto.read.MpaDto;
import ru.yandex.practicum.filmorate.storage.db.dto.update.UpdateMpaRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MpaMapper {

    public static Mpa mapToMpa(NewMpaRequest request) {
        return Mpa.builder().name(request.getName()).build();
    }

    public static Mpa mapToMpa(MpaDto dto) {
        return Mpa.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    public static MpaDto mapToMpaDto(Mpa rating) {
        return MpaDto.builder().id(rating.getId()).name(rating.getName()).build();
    }

    public static Mpa updateMpaFields(Mpa rating, UpdateMpaRequest request) {
        if (request.hasId()) {
            rating.setId(request.getId());
        }
        if (request.hasName()) {
            rating.setName(request.getName());
        }

        return rating;
    }
}
