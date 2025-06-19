package ru.yandex.practicum.filmorate.storage.db.dto.create;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NewGenreRequest {

    private String name;
}
