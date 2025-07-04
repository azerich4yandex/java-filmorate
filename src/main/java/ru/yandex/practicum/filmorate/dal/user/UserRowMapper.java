package ru.yandex.practicum.filmorate.dal.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder().id(rs.getLong("id")).email(rs.getString("email")).login(rs.getString("login"))
                .name(rs.getString("full_name")).birthday(rs.getDate("birthday").toLocalDate()).build();
    }
}
