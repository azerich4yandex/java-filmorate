package ru.yandex.practicum.filmorate.dal.film;

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
                .id(rs.getLong("ID"))
                .name(rs.getString("FULL_NAME"))
                .description(rs.getString("DESCRIPTION"))
                .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                .duration(rs.getInt("DURATION"))
                .build();

        long ratingId = rs.getLong("RATING_ID");
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
