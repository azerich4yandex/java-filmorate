package ru.yandex.practicum.filmorate.dal;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Slf4j
@RequiredArgsConstructor
public class BaseDbStorage<T> {

    protected final JdbcTemplate jdbcTemplate;
    protected final NamedParameterJdbcTemplate namedJdbcTemplate;
    protected final RowMapper<T> mapper;

    protected long insert(String query, Object... params) {
        log.debug("Начало операции вставки данных");

        log.trace("Вызов insert(). SQL : {}  с параметрами {}", query, Arrays.toString(params));

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();

        log.debug("Операция вставки данных завершена");
        return id != null ? id.longValue() : 0L;
    }

    protected boolean insertWithOutReturnId(String query, Object... params) {
        log.debug("Начало операции вставки данных без возврата ключа");

        boolean isRecordInserted;

        log.trace("Вызов insertWithOutReturnId(). SQL : {}  с параметрами {}", query, Arrays.toString(params));

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                for (int idx = 0; idx < params.length; idx++) {
                    ps.setObject(idx + 1, params[idx]);
                }

                return ps;
            });
            isRecordInserted = true;
        } catch (DataAccessException ignored) {
            isRecordInserted = false;
        }

        log.debug("Операция вставки данных без возврата ключа завершена");
        return isRecordInserted;
    }

    protected Collection<T> findMany(String query, Object... params) {
        log.debug("Начало операции поиска коллекции");

        log.trace("Вызов findMany(). SQL : {}  с параметрами {}", query, params);

        Collection<T> result = jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        }, mapper);

        log.debug("Операция поиска коллекции завершена");
        return result;
    }

    protected Collection<T> findManyParametrized(String query, MapSqlParameterSource params) {
        log.debug("Начало вызова поиска коллекции с именованными переменными");
        Collection<T> result = namedJdbcTemplate.query(query, params, mapper);

        log.debug("Операция поиска коллекции с именованными параметрами завершена");
        return result;
    }

    protected Optional<T> findOne(String query, Object... params) {
        log.debug("Начало операции поиска экземпляра");

        log.trace("Вызов findOne(). SQL : {}  с параметрами {}", query, params);

        Collection<T> resultSet = jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        }, mapper);

        log.debug("Операция поиска экземпляра завершена");
        if (resultSet.isEmpty()) {
            return Optional.empty();
        } else {
            return resultSet.stream().findFirst();
        }
    }

    protected boolean exists(String query, Object... params) {
        log.debug("Начало операции проверки наличия сущности");

        log.trace("Вызов exists(). SQL : {}  с параметрами {}", query, params);

        Collection<Long> resultSet = jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        }, (rs, rowNum) -> rs.getLong("id"));

        log.debug("Операция проверки наличия завершена");

        return !(resultSet.isEmpty());

    }

    protected long update(String query, Object... params) {
        log.debug("Начало операции изменения");

        log.debug("Вызов updateFilm(). SQL : {}  с параметрами {}", query, params);

        long result = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        });

        log.debug("Операция изменения завершена");
        return result;
    }

    protected long deleteOne(String query, Object... params) {
        log.debug("Начало операции удаления экземпляра");

        log.debug("Вызов deleteOne(). SQL : {}  с параметрами {}", query, params);

        long result = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        });

        log.debug("Операция удаления экземпляра завершена");
        return result;
    }

    protected long deleteMany(String query, Object... params) {
        log.debug("Начало операции массового удаления");

        log.debug("Вызов deleteMany(). SQL : {}  с параметрами {}", query, params);

        long result = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }

            return ps;
        });

        log.debug("Операция массового удаления завершена");
        return result;
    }
}
