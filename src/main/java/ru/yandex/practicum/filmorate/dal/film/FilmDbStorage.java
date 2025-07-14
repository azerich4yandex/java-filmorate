package ru.yandex.practicum.filmorate.dal.film;

import java.sql.Types;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseDbStorage;
import ru.yandex.practicum.filmorate.dal.genre.GenreStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

@Slf4j
@Component
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String GET_ALL_FILMS_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME as rating_name
              FROM FILMS f
              LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
             ORDER BY f.ID
             LIMIT :size
            OFFSET :from
            """;
    private static final String GET_COMMON_FILMS_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME AS rating_name
              FROM USERS_FILMS uf
             INNER JOIN FILMS f ON uf.FILM_ID = f.ID
              LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
             WHERE  uf.USER_ID = :userId
            INTERSECT
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME AS rating_name
              FROM USERS_FILMS uf
             INNER JOIN FILMS f ON uf.FILM_ID = f.ID
              LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
             WHERE uf.USER_ID = :friendId
            """;
    private static final String GET_POPULAR_FILMS_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.DURATION,
                   f.RELEASE_DATE,
                   f.RATING_ID,
                   r.FULL_NAME AS rating_name,
                   COUNT(uf.ID) AS cnt
              FROM FILMS f
             INNER JOIN RATINGS r ON f.RATING_ID = r.ID
              LEFT JOIN USERS_FILMS uf ON f.ID = uf.FILM_ID
              LEFT JOIN FILMS_GENRES fg ON f.ID = fg.FILM_ID
              LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID
             WHERE (:genreId IS NULL OR g.ID = :genreId)
               AND (:year IS NULL OR YEAR(f.RELEASE_DATE) = :year)
             GROUP BY f.ID, r.ID
             ORDER BY CNT  DESC
             LIMIT :count
            """;
    private static final String GET_ALL_FILMS_BY_GENRE_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME as rating_name
              FROM FILMS_GENRES fg
              LEFT JOIN RATINGS r
                ON f.RATING_ID = r.ID
             INNER JOIN FILMS f
                ON fg.FILM_ID = f.ID
             WHERE fg.GENRE_ID = :genreId
             ORDER BY f.ID
            """;
    private static final String GET_ALL_FILMS_BY_RATING_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.DURATION,
                   f.RELEASE_DATE,
                   f.RATING_ID,
                   r.FULL_NAME AS rating_name
              FROM FILMS f
              LEFT JOIN RATINGS r
                ON f.RATING_ID = r.ID
             WHERE r.ID = :ratingId
            """;
    private static final String GET_FILM_BY_ID_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME as rating_name
              FROM FILMS f
              LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
             WHERE f.ID = :filmId
            """;
    private static final String INSERT_FILM_QUERY = """
            INSERT INTO FILMS(FULL_NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
            VALUES (:filmName, :filmDescription, :filmReleaseDate, :filmDuration, :ratingId)
            """;
    private static final String UPDATE_FILM_QUERY = """
            UPDATE FILMS
               SET FULL_NAME = :filmName,
                   DESCRIPTION = :filmDescription,
                   RELEASE_DATE = :filmReleaseDate,
                   DURATION = :filmDuration,
                   RATING_ID = :ratingId
             WHERE ID = :filmId
            """;
    private static final String GET_LIKE_ID_QUERY = """
            SELECT 1 AS ID
              FROM USERS_FILMS uf
             WHERE uf.FILM_ID = :filmId
               AND uf.USER_ID = :userId
            """;
    private static final String GET_GENRE_AND_FILM_LINK_QUERY = """
            SELECT 1 AS ID
              FROM FILMS_GENRES fg
             WHERE fg.FILM_ID = :filmId
               AND fg.GENRE_ID = :genreId
            """;
    private static final String GET_RATING_ID_ON_FILM_QUERY = """
            SELECT r.ID
              FROM FILMS f
             WHERE f.ID = :filmId
              LEFT JOIN RATINGS r ON f.RATING_ID = r,ID
               AND r.RATING_ID = :ratingId
            """;
    private static final String INSERT_LIKE_QUERY = """
            INSERT INTO USERS_FILMS (FILM_ID, USER_ID)
            VALUES (:filmId, :userId)
            """;
    private static final String INSERT_GENRE_TO_FILM_QUERY = """
            INSERT INTO FILMS_GENRES (FILM_ID, GENRE_ID)
            VALUES (:filmId, :genreId)
            """;
    private static final String INSERT_RATING_ON_FILM_QUERY = """
            UPDATE FILMS
               SET RATING_ID = :ratingId
             WHERE FILM_ID = :filmId
            """;
    private static final String DELETE_LIKE_QUERY = """
            DELETE FROM USERS_FILMS
             WHERE FILM_ID = :filmId
               AND USER_ID = :userId
            """;
    private static final String DELETE_GENRE_ON_FILM_QUERY = """
            DELETE FROM FILMS_GENRES fg
             WHERE fg.FILM_ID = :filmId
               AND fg.GENRE_ID = :genreId
            """;
    private static final String REMOVE_RATING_FROM_FILM_QUERY = """
            UPDATE FILMS
               SET RATING_ID = null
              WHERE ID = :filmId
            """;
    private static final String DELETE_FILM_BY_ID_QUERY = """
            DELETE FROM FILMS
             WHERE ID = :filmId
            """;
    private static final String DELETE_ALL_FILMS_QUERY = """
            DELETE FROM FILMS
            """;

    private final GenreStorage genreStorage;

    @Autowired
    public FilmDbStorage(NamedParameterJdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper,
                         GenreStorage genreStorage) {
        super(jdbcTemplate, filmRowMapper);
        this.genreStorage = genreStorage;
    }

    @Override
    public Collection<Film> findAll(Integer size, Integer from) {
        log.debug("Запрос всех фильмов на уровне хранилища");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("size", size, Types.BIGINT)
                .addValue("from", from, Types.BIGINT);

        // Получаем коллекцию всех фильмов
        Collection<Film> result = findMany(GET_ALL_FILMS_QUERY, parameterSource);
        log.debug("Получена коллекция размером {}", result.size());

        // Возвращаем результат
        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findCommon(Long userId, Long friendId) {
        log.debug("Запрос общих фильмов на уровне хранилища");
        log.debug("Идентификатор первого пользователя: {}", userId);
        log.debug("Идентификатора второго пользователя: {}", friendId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT)
                .addValue("friendId", friendId, Types.BIGINT);

        // Получаем коллекцию общих фильмов
        Collection<Film> result = findMany(GET_COMMON_FILMS_QUERY, parameterSource);
        log.debug("Получено коллекцию общих фильмов размером {}", result.size());

        // Возвращаем результат
        return result;
    }

    @Override
    public Collection<Film> findPopular(Integer count, Long genreId, Integer year) {
        log.debug("Запрос топ фильмов на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", count);
        log.debug("Идентификатор жанра: {}", genreId == null ? "null" : genreId);
        log.debug("Год релиза: {}", year == null ? "null" : year);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("count", count, Types.BIGINT)
                .addValue("year", year, Types.INTEGER)
                .addValue("genreId", genreId, Types.BIGINT);

        // Получаем коллекцию популярных фильмов
        Collection<Film> result = findMany(GET_POPULAR_FILMS_QUERY, parameterSource);
        log.debug("На уровне сервиса получена коллекция размером {}", result.size());

        // Возвращаем результат
        log.debug("Возврат результатов на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findByGenreId(Long genreId) {
        log.debug("Запрос списка фильмов по жанру");
        log.debug("Идентификатор запрашиваемого жанра: {}", genreId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("genreId", genreId, Types.BIGINT);

        Collection<Film> result = findMany(GET_ALL_FILMS_BY_GENRE_QUERY, parameterSource);
        log.debug("Получена коллекция фильмов размером {}", result.size());

        log.debug("Возврат результатов поиска по жанру на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findByRatingId(Long ratingId) {
        log.debug("Запрос списка фильмов по рейтингу");
        log.debug("Идентификатор запрашиваемого рейтинга: {}", ratingId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("ratingId", ratingId, Types.BIGINT);

        Collection<Film> result = findMany(GET_ALL_FILMS_BY_RATING_QUERY, parameterSource);
        log.debug("Получена коллекция фильмов по жанру размером {}", result.size());

        log.debug("Возврат результатов поиска по рейтингу на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        log.debug("Поиск фильма по id на уровне хранилища");

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT);

        Optional<Film> searchResult = findOne(GET_FILM_BY_ID_QUERY, parameterSource);
        if (searchResult.isPresent()) {
            Film result = searchResult.get();

            log.debug("Возврат результата поиска на уровень сервиса");
            return Optional.of(result);
        } else {
            log.debug("Возврат пустого результата поиска на уровень сервиса");
            return Optional.empty();
        }
    }

    @Override
    public Film createFilm(Film film) {
        log.debug("Создание фильма на уровне хранилища");

        Long ratingId = film.getMpa() != null && film.getMpa().getId() != null ? film.getMpa().getId() : null;

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmName", film.getName(), Types.NVARCHAR)
                .addValue("filmDescription", film.getDescription(), Types.NVARCHAR)
                .addValue("filmReleaseDate", film.getReleaseDate(), Types.DATE)
                .addValue("filmDuration", film.getDuration(), Types.INTEGER)
                .addValue("ratingId", ratingId, Types.BIGINT);

        long id = insert(INSERT_FILM_QUERY, parameterSource);

        if (id == 0) {
            throw new RuntimeException("Не удалось сохранить фильм в БД");
        } else {
            log.debug("Сгенерировано значение {}", id);
        }

        film.setId(id);
        log.debug("Значение id присвоено фильму");

        propagateModel(film);

        log.debug("Возврат результатов создания на уровень сервиса");
        return film;
    }

    @Override
    public void updateFilm(Film newFilm) {
        log.debug("Изменение фильма на уровне хранилища");

        Long ratingId = newFilm.getMpa() != null && newFilm.getMpa().getId() != null ? newFilm.getMpa().getId() : null;

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmName", newFilm.getName(), Types.NVARCHAR)
                .addValue("filmDescription", newFilm.getDescription(), Types.NVARCHAR)
                .addValue("filmReleaseDate", newFilm.getReleaseDate(), Types.DATE)
                .addValue("filmDuration", newFilm.getDuration(), Types.INTEGER)
                .addValue("ratingId", ratingId, Types.BIGINT)
                .addValue("filmId", newFilm.getId(), Types.BIGINT);

        long updatedRows = update(UPDATE_FILM_QUERY, parameterSource);

        if (updatedRows == 0) {
            throw new RuntimeException("Не удалось обновить фильм с id " + newFilm.getId());
        } else {
            log.debug("На уровне хранилища обновлено {} запись(ей)", updatedRows);
        }

        propagateModel(newFilm);

        log.debug("Возврат результатов изменения на уровень сервиса");
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка на уровне хранилища");
        log.debug("Идентификатор фильма: {}", filmId);
        log.debug("Идентификатор пользователя: {}", userId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("userId", userId, Types.BIGINT);

        if (!exists(GET_LIKE_ID_QUERY, parameterSource)) {
            log.debug("Лайк будет добавлен в БД");

            boolean isInserted = insertWithOutReturnId(INSERT_LIKE_QUERY, parameterSource);
            if (!isInserted) {
                throw new RuntimeException(
                        "Не удалось сохранить лайк от пользователя с id " + userId + " фильму с id " + filmId);
            } else {
                log.debug("Фильму с id {} добавлен лайк от пользователя с id {}", filmId, userId);
            }
        } else {
            log.debug("Лайк не будет добавлен в БД, т.к. уже существует");
        }

        log.debug("Возврат результата добавления лайка на уровень сервиса");
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка на уровне хранилища");
        log.debug("Идентификатор  фильма: {}", filmId);
        log.debug("Идентификатор  пользователя: {}", userId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("userId", userId, Types.BIGINT);

        if (exists(GET_LIKE_ID_QUERY, parameterSource)) {
            log.debug("Лайк будет удалён из БД");

            long deletedRows = deleteOne(DELETE_LIKE_QUERY, parameterSource);

            if (deletedRows == 0) {
                throw new RuntimeException(
                        "Не удалось удалить лайк от пользователя с id " + userId + " с фильма с id " + filmId);
            }

            log.debug("Лайк от пользователя с id {} снят с фильма с id {}", userId, filmId);
        } else {
            log.debug("Лайк не будет удалён из БД, так как не существует");
        }

        log.debug("Возврат результата удаления лайка на уровень сервиса");
    }

    @Override
    public void addGenre(Long filmId, Long genreId) {
        log.debug("Добавление жанра фильму на уровне хранилища");
        log.debug("Переданный идентификатор фильма: {}", filmId);
        log.debug("Переданный идентификатор жанра: {}", genreId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("genreId", genreId, Types.BIGINT);

        if (!exists(GET_GENRE_AND_FILM_LINK_QUERY, parameterSource)) {
            log.debug("Жанр будет добавлен фильму");

            boolean isInserted = insertWithOutReturnId(INSERT_GENRE_TO_FILM_QUERY, parameterSource);
            if (!isInserted) {
                throw new RuntimeException("Не удалось добавить жанр с id " + genreId + " фильму с id " + filmId);
            } else {
                log.debug("Фильму с id {} добавлен жанр с id {}", filmId, genreId);
            }
        } else {
            log.debug("Жанр не будет добавлен фильму, т.к. уже существует");
        }

        log.debug("Возврат результатов добавления жанра на уровень сервиса");
    }

    @Override
    public void removeGenre(Long filmId, Long genreId) {
        log.debug("Удаление жанра из фильма на уровне хранилища");
        log.debug("Переданный идентификатор изменяемого фильма: {}", filmId);
        log.debug("Переданный идентификатор удаляемого жанра: {}", genreId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("genreId", genreId, Types.BIGINT);

        if (!exists(GET_GENRE_AND_FILM_LINK_QUERY, parameterSource)) {
            long deletedRows = deleteOne(DELETE_GENRE_ON_FILM_QUERY, parameterSource);

            if (deletedRows == 0) {
                throw new RuntimeException(
                        "Не удалось удалить связь жанра с id " + genreId + "и фильма с id " + filmId);
            }
            log.debug("Фильм с id {} больше не принадлежит жанру с id {}", filmId, genreId);
        } else {
            log.debug("Жанр не будет удалён из фильма, т.к. отсутствует в БД");
        }

        log.debug("Возврат результата удаления жанра на уровень сервиса");
    }

    @Override
    public void addRating(Long filmId, Long ratingId) {
        log.debug("Установка рейтинга фильму на уровне хранилища");
        log.debug("Идентификатор переданного фильма: {}", filmId);
        log.debug("Идентификатор переданного жанра: {}", ratingId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("ratingId", ratingId, Types.BIGINT);

        if (!exists(GET_RATING_ID_ON_FILM_QUERY, parameterSource)) {
            long updatedRows = update(INSERT_RATING_ON_FILM_QUERY, parameterSource);

            if (updatedRows == 0) {
                throw new RuntimeException("Не удалось установить рейтинг с id " + ratingId + "фильму с id " + filmId);
            } else {
                log.debug("На уровне хранилища обновлено  {} запись(ей)", updatedRows);
            }

            log.debug("Фильму с id {} присвоен рейтинг с id {}", filmId, ratingId);
        } else {
            log.debug("Рейтинг не будет установлен фильму, т.к. уже установлен");
        }

        log.debug("Возврат результата установки на уровень сервиса");
    }

    @Override
    public void removeRating(Long filmId) {
        log.debug("Удаление рейтинга с фильма на уровне хранилища");
        log.debug("Передан id изменяемого фильма: {}", filmId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT);

        long updatedRows = update(REMOVE_RATING_FROM_FILM_QUERY, parameterSource);

        if (updatedRows == 0) {
            throw new RuntimeException("Не удалось очистить рейтинг у фильма с id " + filmId);
        } else {
            log.debug("На уровне хранилища обновлено {} запись(ей) ", updatedRows);
        }
        log.debug("С фильма с id {} снят рейтинг", filmId);

        log.debug("Возврат результатов удаления рейтинга на уровень сервиса");
    }

    @Override
    public void deleteFilm(Long filmId) {
        log.debug("Удаление фильма на уровне хранилища");
        log.debug("Передан id фильма: {}", filmId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT);

        long deletedRows = deleteOne(DELETE_FILM_BY_ID_QUERY, parameterSource);

        if (deletedRows == 0) {
            throw new RuntimeException("Не удалось удалить фильм с id " + filmId);
        }

        log.debug("Фильм с id {} удален из хранилища", filmId);

        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearFilms() {
        log.debug("Очистка хранилища фильмов");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        long deletedRows = deleteMany(DELETE_ALL_FILMS_QUERY, parameterSource);
        log.debug("На уровне хранилища удалено {} записей(ь)", deletedRows);

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    /**
     * Метод распространяет коллекции переданного фильма по структурам БД
     *
     * @param film экземпляр класса {@link Film}
     */
    private void propagateModel(Film film) {
        log.debug("Распространение коллекций фильма");

        // Распространяем коллекцию жанров
        propagateGenres(film);
    }

    /**
     * Метод распространяет коллекцию жанров по структурам БД
     *
     * @param film экземпляр класса {@link Film}
     */
    private void propagateGenres(Film film) {
        if (!film.getGenres().isEmpty()) {
            log.debug("Найдена коллекция жанров");
            clearGenres(film);

            log.debug("Добавление нового списка жанров");
            for (Long genreId : film.getGenres()) {
                addGenre(film.getId(), genreId);
            }
            log.debug("Добавление списка завершено");
        } else {
            log.debug("Передана пустая коллекция жанров. Будет произведена очистка связей на уровне БД");
            clearGenres(film);
        }
    }

    /**
     * Метод очищает коллекцию жанров фильма в БД
     *
     * @param film экземпляр класса {@link Film}
     */
    private void clearGenres(Film film) {
        log.debug("Очистка имеющихся жанров");

        Collection<Long> genreIds = genreStorage.findByFilmId(film.getId()).stream().map(Genre::getId).toList();

        if (!genreIds.isEmpty()) {
            log.debug("В БД найдены связи, подлежащие удалению");
            for (Long genreId : genreIds) {
                removeGenre(film.getId(), genreId);
            }
        }
        log.debug("Очистка завершена");
    }
}