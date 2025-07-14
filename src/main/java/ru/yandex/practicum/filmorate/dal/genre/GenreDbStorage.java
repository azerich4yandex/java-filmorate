package ru.yandex.practicum.filmorate.dal.genre;

import java.sql.Types;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

@Slf4j
@Component
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {

    private static final String GET_ALL_GENRES_QUERY = """
            SELECT g.ID,
                   g.FULL_NAME
              FROM GENRES g
             ORDER BY g.ID
             LIMIT :size
            OFFSET :from
            """;
    private static final String GET_GENRES_BY_FILM_ID_QUERY = """
            SELECT g.ID,
                   g.FULL_NAME
              FROM FILMS_GENRES fg
             INNER JOIN GENRES g on fg.GENRE_ID = g.ID
             WHERE fg.FILM_ID = :filmId
             ORDER BY g.ID
            """;
    private static final String GET_GENRE_BY_ID_QUERY = """
            SELECT g.ID,
                   g.FULL_NAME
              FROM GENRES g
             WHERE g.ID = :genreId
            """;
    private static final String INSERT_GENRE_QUERY = """
            INSERT INTO GENRES (FULL_NAME)
            VALUES (:genreName)
            """;
    private static final String UPDATE_GENRE_QUERY = """
            UPDATE GENRES
               SET FULL_NAME = :genreName
             WHERE ID = :genreId
            """;

    private static final String GET_GEN_NAME_BEFORE_UPDATE_QUERY = """
            SELECT g.ID
              FROM GENRES g
             WHERE UPPER(g.FULL_NAME) = :genreName
               AND g.ID != :genreId
            """;
    private static final String GET_GEN_NAME_BEFORE_INSERT_QUERY = """
            SELECT g.ID
              FROM GENRES g
             WHERE UPPER(g.FULL_NAME) = :genreName
            """;
    private static final String DELETE_GENRE_BY_ID_QUERY = """
            DELETE FROM GENRES g
             WHERE g.ID = :genreId
            """;
    private static final String DELETE_ALL_GENRES_QUERY = """
            DELETE FROM GENRES
            """;

    @Autowired
    public GenreDbStorage(NamedParameterJdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        super(jdbcTemplate, genreRowMapper);
    }

    @Override
    public Collection<Genre> findAll(Integer size, Integer from) {
        log.debug("Запрос всех жанров на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("size", size)
                .addValue("from", from);

        Collection<Genre> result = findMany(GET_ALL_GENRES_QUERY, parameterSource);
        log.debug("Получена коллекция всех жанров размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Genre> findByFilmId(Long filmId) {
        log.debug("Запрос жанров по идентификатору фильма на уровне хранилища");
        log.debug("Идентификатор запрашиваемого фильма: {}", filmId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT);

        Collection<Genre> result = findMany(GET_GENRES_BY_FILM_ID_QUERY, parameterSource);
        log.debug("Получена коллекция размером {}", result.size());

        log.debug("Возврат результатов на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Genre> findById(Long genreId) {
        log.debug("Поиск жанра по id на уровне хранилища");
        log.debug("Идентификатор запрашиваемого жанра: {}", genreId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("genreId", genreId);

        Optional<Genre> result = findOne(GET_GENRE_BY_ID_QUERY, parameterSource);

        log.debug("Возврат результата поиска на уровень сервиса");
        return result;
    }

    @Override
    public Genre createGenre(Genre genre) {
        log.debug("Создание жанра на уровне хранилища");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("genreName", genre.getName());

        long id = insert(INSERT_GENRE_QUERY, parameterSource);

        if (id == 0) {
            throw new RuntimeException("Не удалось добавить жанр в БД");
        } else {
            log.debug("Сгенерировано значение {}", id);
        }

        genre.setId(id);
        log.debug("Значение присвоено id присвоено жанру");

        log.debug("Возврат результатов создания на уровень сервиса");
        return genre;
    }

    @Override
    public void updateGenre(Genre newGenre) {
        log.debug("Изменение жанра на уровне хранилища");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("genreName", newGenre.getName())
                .addValue("genreId", newGenre.getId());

        long updatedRows = update(UPDATE_GENRE_QUERY, parameterSource);

        if (updatedRows == 0) {
            throw new RuntimeException("Не удалось обновить жанр с id " + newGenre.getId());
        } else {
            log.debug("На уровне хранилища обновлено {} запись(ей)", updatedRows);
        }

        log.debug("Возврат результатов изменения на уровень сервиса");
    }

    @Override
    public void deleteGenre(Long genreId) {
        log.debug("Удаление жанра на уровне хранилища");
        log.debug("Передан id жанра: {}", genreId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("genreId", genreId);

        long deletedRows = deleteOne(DELETE_GENRE_BY_ID_QUERY, parameterSource);

        if (deletedRows == 0) {
            throw new RuntimeException("Не удалось удалить жанр с id " + genreId);
        }

        log.debug("Жанр с id {} удален из хранилища", genreId);

        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearGenres() {
        log.debug("Очистка хранилища жанров");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        long deletedRows = deleteMany(DELETE_ALL_GENRES_QUERY, parameterSource);
        log.debug("На уровне хранилища удалено {} запись(ей)", deletedRows);

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    @Override
    public boolean isNameAlreadyUsed(Genre genre) {
        log.debug("Проверка на использование наименования жанра {} другими жанрами", genre.getName());

        if (genre.getId() != null) {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("genreName", genre.getName().toUpperCase())
                    .addValue("genreId", genre.getId());

            return exists(GET_GEN_NAME_BEFORE_UPDATE_QUERY, parameterSource);

        } else {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("genreName", genre.getName().toUpperCase());

            return exists(GET_GEN_NAME_BEFORE_INSERT_QUERY, parameterSource);
        }
    }
}