package ru.yandex.practicum.filmorate.dal.director;

import java.sql.Types;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseDbStorage;
import ru.yandex.practicum.filmorate.model.Director;

@Slf4j
@Component
public class DirectorDbStorage extends BaseDbStorage<Director> implements DirectorStorage {

    private static final String GET_ALL_DIRECTORS_QUERY = """
            SELECT d.ID,
            	   d.FULL_NAME
              FROM DIRECTORS d
             LIMIT :size
            OFFSET :from
            """;
    private static final String GET_DIRECTORS_BY_FILM_ID_QUERY = """
            SELECT d.ID,
            	   d.FULL_NAME
              FROM FILMS_DIRECTORS fd
             INNER JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.ID
             WHERE fd.FILM_ID = :filmId
            """;
    private static final String GET_DIRECTOR_BY_ID_QUERY = """
            SELECT d.ID,
            	   d.FULL_NAME
              FROM DIRECTORS d
             WHERE d.ID = :directorId
            """;
    private static final String INSERT_DIRECTOR_QUERY = """
            INSERT INTO DIRECTORS(FULL_NAME)
            VALUES (:directorName)
            """;
    private static final String UPDATE_DIRECTOR_QUERY = """
            UPDATE DIRECTORS d
               SET d.FULL_NAME = :directorName
             WHERE d.ID = :directorId
            """;
    private static final String DELETE_DIRECTOR_BY_ID_QUERY = """
            DELETE FROM DIRECTORS
             WHERE ID = :directorId
            """;
    private static final String DELETE_ALL_DIRECTORS_QUERY = """
            DELETE FROM DIRECTORS
            """;

    @Autowired
    public DirectorDbStorage(NamedParameterJdbcTemplate jdbcTemplate,
                             RowMapper<Director> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Collection<Director> findAll(Integer size, Integer from) {
        log.debug("Запрос всех режиссеров на уровне хранилища");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("size", size, Types.INTEGER)
                .addValue("from", from, Types.INTEGER);

        Collection<Director> result = findMany(GET_ALL_DIRECTORS_QUERY, parameterSource);
        log.debug("Получена коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Director> findByFilmId(Long filmId) {
        log.debug("Запрос всех режиссеров фильма на уровне хранилища");
        log.debug("Передан идентификатор фильма: {}", filmId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT);

        Collection<Director> result = findMany(GET_DIRECTORS_BY_FILM_ID_QUERY, parameterSource);
        log.debug("На уровне хранилища получена коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска режиссеров фильма на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Director> findById(Long directorId) {
        log.debug("Запрос режиссер по id на уровне хранилища");
        log.debug("Передан id режиссера: {}", directorId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("directorId", directorId, Types.BIGINT);

        Optional<Director> result = findOne(GET_DIRECTOR_BY_ID_QUERY, parameterSource);

        log.debug("Возврат результатов поиска режиссера на уровень сервиса");
        return result;
    }

    @Override
    public Director createDirector(Director director) {
        log.debug("Запрос на создание режиссера на уровне хранилища");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("directorName", director.getName(), Types.NVARCHAR);

        long id = insert(INSERT_DIRECTOR_QUERY, parameterSource);

        if (id == 0) {
            throw new RuntimeException("Не удалось сохранить режиссера в БД");
        } else {
            log.debug("Сгенерировано значение {}", id);
        }

        director.setId(id);
        log.debug("Значение id присвоено режиссеру");

        return director;
    }

    @Override
    public void updateDirector(Director newDirector) {
        log.debug("Запрос на изменение режиссера на уровне хранилища");
        log.debug("Передан id  режиссера: {}", newDirector.getId());

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("directorName", newDirector.getName(), Types.NVARCHAR)
                .addValue("directorId", newDirector.getId(), Types.BIGINT);

        long updatedRows = update(UPDATE_DIRECTOR_QUERY, parameterSource);

        if (updatedRows == 0) {
            throw new RuntimeException("Не удалось обновить фильм с id " + newDirector.getId());
        } else {
            log.debug("На уровне хранилища обновлено {} запись(ей)", updatedRows);
        }

        log.debug("Возврат результатов на уровень сервиса");
    }

    @Override
    public void deleteDirector(Long directorId) {
        log.debug("Запрос на удаление режиссера на уровне хранилища");
        log.debug("Передан идентификатор  режиссера: {}", directorId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("directorId", directorId, Types.BIGINT);

        long deletedRows = deleteOne(DELETE_DIRECTOR_BY_ID_QUERY, parameterSource);
        log.debug("На уровне хранилища удалено {} запись(ей)", deletedRows);
        log.debug("Режиссер с id {} удален из хранилища", directorId);

        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearDirectors() {
        log.debug("Запрос на очистку хранилища режиссеров");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        long deletedRows = deleteMany(DELETE_ALL_DIRECTORS_QUERY, parameterSource);
        log.debug("На уровне хранилища очищено {} запись(ей)", deletedRows);

        log.debug("Возврат результатов очистки на уровень сервиса");
    }
}
