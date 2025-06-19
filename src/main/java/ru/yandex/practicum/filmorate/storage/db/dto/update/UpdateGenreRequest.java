package ru.yandex.practicum.filmorate.storage.db.dto.update;

import lombok.Data;

@Data
public class UpdateGenreRequest {

    private Long id;
    private String name;

    public boolean hasId() {
        return id != null && id > 0;
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
