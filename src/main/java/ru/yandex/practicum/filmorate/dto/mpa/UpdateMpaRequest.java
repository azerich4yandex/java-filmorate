package ru.yandex.practicum.filmorate.dto.mpa;

import lombok.Data;

@Data
public class UpdateMpaRequest {

    private Long id;
    private String name;

    public boolean hasId() {
        return id != null && id > 0;
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }
}
