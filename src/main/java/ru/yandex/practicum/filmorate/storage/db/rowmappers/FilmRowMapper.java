package ru.yandex.practicum.filmorate.storage.db.rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

@Slf4j
@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film result = Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("full_name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .build();

        long ratingId = rs.getLong("rating_id");
        if (ratingId > 0) {
            Mpa rating = Mpa.builder()
                    .id(ratingId)
                    .name(rs.getString("rating_name"))
                    .build();
            result.setMpa(rating);
        }

        return result;
    }
}
