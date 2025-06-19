package ru.yandex.practicum.filmorate.dto.mpa;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MpaDto {

    private Long id;
    private String name;
}
