package ru.yandex.practicum.filmorate.dto.director;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateDirectorRequest {

    private Long id;
    private String name;

    public boolean hasId() {
        return id != null && id > 0;
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
