package ru.yandex.practicum.filmorate.dal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Slf4j
@RequiredArgsConstructor
public class BaseDbStorage<T> {

    protected final NamedParameterJdbcTemplate jdbcTemplate;
    protected final RowMapper<T> mapper;

    protected long insert(String query, MapSqlParameterSource params) {
        log.debug("Начало операции вставки данных с именованными параметрами");

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(query, params, keyHolder);

        Number id = keyHolder.getKey();

        log.debug("Операция вставки данных с именованными параметрами завершена");
        return id != null ? id.longValue() : 0L;
    }

    protected boolean insertWithOutReturnId(String query, MapSqlParameterSource params) {
        log.debug("Начало операции вставки данных без возврата ключа с именованными параметрами");

        long insertedRecords;

        try {
            insertedRecords = jdbcTemplate.update(query, params);
        } catch (DataAccessException ignored) {
            insertedRecords = 0;
        }

        log.debug("Операция вставки данных без возврата ключа с именованными параметрами завершена");
        return insertedRecords == 1;
    }

    protected Collection<T> findMany(String query, MapSqlParameterSource params) {
        log.debug("Начало вызова поиска коллекции с именованными переменными");
        Collection<T> result = jdbcTemplate.query(query, params, mapper);

        log.debug("Операция поиска коллекции с именованными параметрами завершена");
        return result;
    }

    protected Optional<T> findOne(String query, MapSqlParameterSource params) {
        log.debug("Начало вызова поиск экземпляра с именованными переменными");

        T result;

        try {
            result = jdbcTemplate.queryForObject(query, params, mapper);
        } catch (DataAccessException ignored) {
            result = null;
        }

        log.debug("Операция поиска экземпляра с именованными параметрами завершена");
        if (result != null) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

    protected boolean exists(String query, MapSqlParameterSource params) {
        log.debug("Начало операции проверки сущностей с именованными параметрами");

        Collection<Long> resultSet;
        try {
            resultSet = jdbcTemplate.query(query, params, (ResultSetExtractor<Collection<Long>>) rs -> {
                List<Long> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getLong("id"));
                }
                return result;
            });
        } catch (DataAccessException ignored) {
            resultSet = new ArrayList<>();
        }

        log.debug("Операция проверки наличия сущностей с именованными параметрами завершена");
        return resultSet != null && !resultSet.isEmpty();
    }

    protected long update(String query, MapSqlParameterSource params) {
        log.debug("Начало операции изменения с именованными параметрами");

        long result = jdbcTemplate.update(query, params);

        log.debug("Операция изменения с именованными параметрами завершена");
        return result;
    }

    protected long deleteOne(String query, MapSqlParameterSource params) {
        log.debug("Начало операции удаления с именованными параметрами");

        long result = jdbcTemplate.update(query, params);

        log.debug("Операция удаления с именованными параметрами завершена");
        return result;
    }

    protected long deleteMany(String query, MapSqlParameterSource params) {
        log.debug("Начало операции массового удаления с именованными параметрами");

        long result = jdbcTemplate.update(query, params);

        log.debug("Операция массового удаления с именованными параметрами завершена");
        return result;
    }
}
