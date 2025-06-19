package ru.yandex.practicum.filmorate.storage.db.dto.read;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MpaDto implements Comparable<GenreDto> {

    private Long id;
    private String name;

    @Override
    public int compareTo(GenreDto o) {
        if (this.id < o.getId()) {
            return -1;
        } else if (this.id > o.getId()) {
            return 1;
        } else {
            return 0;
        }
    }
}
