package ru.yandex.practicum.filmorate.dal.film;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseDbStorage;
import ru.yandex.practicum.filmorate.dal.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dal.genre.GenreStorage;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

@Slf4j
@Component
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final Integer MIN_NEGATIVE_RATE = 1;
    private static final Integer MAX_NEGATIVE_RATE = 5;
    private static final Integer MAX_POSITIVE_RATE = 10;
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
            	   AVG(uf.MARK) AS rate
              FROM FILMS f
             INNER JOIN RATINGS r ON f.RATING_ID = r.ID
              LEFT JOIN USERS_FILMS uf ON f.ID = uf.FILM_ID
              LEFT JOIN FILMS_GENRES fg ON f.ID = fg.FILM_ID
              LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID
             WHERE (:genreId IS NULL OR g.ID = :genreId)
               AND (:year IS NULL OR YEAR(f.RELEASE_DATE) = :year)
             GROUP BY f.ID, r.ID
             ORDER BY rate DESC
             LIMIT :count
            """;
    private static final String GET_FILMS_WITH_LIKE_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME AS rating_name,
                   NVL(COUNT(uf.USER_ID), 0) AS likes
              FROM FILMS f
              LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
              LEFT JOIN USERS_FILMS uf ON f.ID = uf.FILM_ID
              LEFT JOIN FILMS_DIRECTORS fd ON f.ID = fd.FILM_ID
              LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.ID
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
    private static final String GET_FILMS_BY_DIRECTOR_ID = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME as rating_name,
                   NVL(COUNT(uf.ID), 0) AS likes,
                   AVG(uf.MARK) AS rate
              FROM FILMS f
             INNER JOIN FILMS_DIRECTORS fd ON f.ID = fd.FILM_ID
             INNER JOIN DIRECTORS d ON d.ID = fd.DIRECTOR_ID
              LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
              LEFT JOIN USERS_FILMS uf ON f.ID = uf.FILM_ID
             WHERE d.ID = :directorId
             GROUP BY f.ID,
                      f.FULL_NAME,
                      f.DESCRIPTION,
                      f.RELEASE_DATE,
                      f.DURATION,
                      f.RATING_ID,
                      r.FULL_NAME
            """;
    private static final String GET_RECOMMENDED_FILMS_QUERY = """
            SELECT f.ID,
                   f.FULL_NAME,
                   f.DESCRIPTION,
                   f.RELEASE_DATE,
                   f.DURATION,
                   f.RATING_ID,
                   r.FULL_NAME as rating_name,
                   COUNT(uf4.USER_ID) AS likes,
                   NVL(AVG(uf4.MARK), 0) AS rate
              FROM USERS_FILMS uf1
              INNER JOIN USERS_FILMS uf2 ON uf1.FILM_ID = uf2.FILM_ID
              INNER JOIN USERS_FILMS uf3 ON uf2.USER_ID = uf3.USER_ID
              INNER JOIN FILMS f ON uf3.FILM_ID = f.ID
               LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
               LEFT JOIN USERS_FILMS uf4 ON f.ID = uf4.FILM_ID
             WHERE uf1.USER_ID = :userId
                --
               AND (((uf1.MARK >= :minNegativeRate AND uf1.MARK <= :maxNegativeRate) AND (uf2.MARK >= :minNegativeRate AND uf2.MARK <= :maxNegativeRate))
                OR  ((uf1.MARK > :maxNegativeRate AND uf1.MARK <= :maxPositiveRate) AND (uf2.MARK > :maxNegativeRate AND uf2.MARK <= :maxPositiveRate))
                OR  (uf1.MARK IS NULL AND uf2.MARK IS NULL))
               AND uf2.USER_ID != uf1.USER_ID
                --
               AND (((uf2.MARK >= :minNegativeRate AND uf2.MARK <= :maxNegativeRate) AND (uf3.MARK >= :minNegativeRate AND uf3.MARK <= :maxNegativeRate))
                OR  ((uf2.MARK > :maxNegativeRate AND uf2.MARK <= :maxPositiveRate) AND (uf3.MARK > :maxNegativeRate AND uf3.MARK <= :maxPositiveRate))
                OR  (uf2.MARK IS NULL AND uf3.MARK IS NULL))
               AND uf3.FILM_ID NOT IN (SELECT uf.FILM_ID FROM USERS_FILMS uf WHERE uf.USER_ID = uf1.USER_ID)
             GROUP BY f.ID,
                      r.ID
            HAVING (AVG(uf4.MARK) > :maxNegativeRate OR uf4.MARK IS NULL)
             ORDER BY rate DESC
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
    private static final String GET_DIRECTOR_AND_FILM_LINK_QUERY = """
            SELECT 1 AS ID
              FROM FILMS_DIRECTORS fd
             WHERE fd.FILM_ID = :filmId
               AND fd.DIRECTOR_ID = :directorId
            """;
    private static final String INSERT_LIKE_QUERY = """
            INSERT INTO USERS_FILMS (FILM_ID, USER_ID, MARK)
            VALUES (:filmId, :userId, :mark)
            """;
    private static final String INSERT_GENRE_TO_FILM_QUERY = """
            INSERT INTO FILMS_GENRES (FILM_ID, GENRE_ID)
            VALUES (:filmId, :genreId)
            """;
    private static final String INSERT_DIRECTOR_TO_FILM_QUERY = """
            INSERT INTO FILMS_DIRECTORS(FILM_ID, DIRECTOR_ID)
            VALUES (:filmId, :directorId)
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
    private static final String DELETE_DIRECTOR_ON_FILM_QUERY = """
            DELETE FROM FILMS_DIRECTORS fg
             WHERE fg.FILM_ID = :filmId
               AND fg.DIRECTOR_ID = :directorId
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
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmDbStorage(NamedParameterJdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper,
                         GenreStorage genreStorage, DirectorStorage directorStorage) {
        super(jdbcTemplate, filmRowMapper);
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
    }

    @Override
    public Collection<Film> findAll(Integer size, Integer from) {
        log.debug("Запрос всех фильмов на уровне хранилища");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource().addValue("size", size)
                .addValue("from", from);

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
        MapSqlParameterSource parameterSource = new MapSqlParameterSource().addValue("userId", userId)
                .addValue("friendId", friendId);

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
        MapSqlParameterSource parameterSource = new MapSqlParameterSource().addValue("count", count)
                .addValue("year", year).addValue("genreId", genreId);

        // Получаем коллекцию популярных фильмов
        Collection<Film> result = findMany(GET_POPULAR_FILMS_QUERY, parameterSource);
        log.debug("На уровне хранилища получена коллекция размером {}", result.size());

        // Возвращаем результат
        log.debug("Возврат результатов на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findSearchResult(String query, String by) {
        log.debug("Запрос списка фильмов по подстроке на уровне хранилища");
        log.debug("Передано значение query: {}", query);
        log.debug("Передано значение by: {}", by);

        // Создаём пустой набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        // Создаём строку запроса
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(GET_FILMS_WITH_LIKE_QUERY);
        stringBuilder.append(" WHERE");

        // Разделяем набор полей
        String[] fields = by.split(",");

        // Получаем список условий для созданного набора полей
        List<String> clauses = getClauses(fields, query);

        // Если список полученных условий пуст
        if (clauses.isEmpty()) {
            log.debug("Во время интерпретации полей не получено условий. Возвращаем пустую коллекцию");
            // Возвращаем пустой список
            return new ArrayList<>();
        }

        // Иначе каждое условие добавляем в запрос
        for (int i = 0; i < clauses.size(); i++) {
            if (i == 0) {
                stringBuilder.append(" ").append(clauses.get(i));
            } else {
                stringBuilder.append("\n   OR ").append(clauses.get(i));
            }
        }

        // Добавляем группировку
        stringBuilder.append(
                "\n GROUP BY f.ID, f.FULL_NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.RATING_ID, r.FULL_NAME");

        // Заканчиваем строку запроса сортировкой
        stringBuilder.append("\n ORDER BY likes DESC");

        Collection<Film> result = findMany(stringBuilder.toString(), parameterSource);
        log.debug("На уровне хранилища получена коллекция фильмов размером {}", result.size());

        log.debug("Возврат результатов поиска по подстроке на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findByGenreId(Long genreId) {
        log.debug("Запрос списка фильмов по жанру");
        log.debug("Идентификатор запрашиваемого жанра: {}", genreId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource().addValue("genreId", genreId);

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
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("ratingId", ratingId);

        Collection<Film> result = findMany(GET_ALL_FILMS_BY_RATING_QUERY, params);
        log.debug("Получена коллекция фильмов по жанру размером {}", result.size());

        log.debug("Возврат результатов поиска по рейтингу на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findByDirectorId(Long directorId, String sortBy) {
        log.debug("Запрос списка фильмов по режиссеру на уровне хранилища");
        log.debug("Передан id режиссера: {}", directorId);
        log.debug("Передана последовательность полей сортировки: {}",
                (sortBy == null || sortBy.isBlank()) ? "null" : sortBy);

        String orderBy = "";

        if (!(sortBy == null || sortBy.isBlank())) {
            String[] fields = sortBy.split(",");
            for (String field : fields) {
                orderBy = addField(orderBy, field.toUpperCase());
            }
        } else {
            orderBy = "LIKES DESC";
        }

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("directorId", directorId);

        Collection<Film> result = findMany(GET_FILMS_BY_DIRECTOR_ID + "\n ORDER BY " + orderBy, parameterSource);
        log.debug("Получена коллекция фильмов по режиссеру размером {}", result.size());

        log.debug("Возврат результатов поиска по режиссеру на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findUserRecommendations(Long userId) {
        log.debug("Поиск рекомендованных фильмов на уровне хранилища");
        log.debug("Передан идентификатор пользователя: {}", userId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT)
                .addValue("minNegativeRate", MIN_NEGATIVE_RATE, Types.BIGINT)
                .addValue("maxNegativeRate", MAX_NEGATIVE_RATE, Types.BIGINT)
                .addValue("maxPositiveRate", MAX_POSITIVE_RATE, Types.INTEGER);

        Collection<Film> result = findMany(GET_RECOMMENDED_FILMS_QUERY, parameterSource);
        log.debug("Получена коллекция рекомендованных фильмов размером {}", result.size());

        log.debug("Возврат результатов поиска рекомендованных фильмов на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        log.debug("Поиск фильма по id на уровне хранилища");

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource().addValue("filmId", filmId);

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
                .addValue("filmName", film.getName())
                .addValue("filmDescription", film.getDescription())
                .addValue("filmReleaseDate", film.getReleaseDate())
                .addValue("filmDuration", film.getDuration())
                .addValue("ratingId", ratingId);

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
                .addValue("filmName", newFilm.getName())
                .addValue("filmDescription", newFilm.getDescription())
                .addValue("filmReleaseDate", newFilm.getReleaseDate())
                .addValue("filmDuration", newFilm.getDuration())
                .addValue("ratingId", ratingId)
                .addValue("filmId", newFilm.getId());

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
    public void addLike(Long filmId, Long userId, Double mark) {
        log.debug("Добавление лайка на уровне хранилища");
        log.debug("Идентификатор фильма: {}", filmId);
        log.debug("Идентификатор пользователя: {}", userId);
        log.debug("Оценка фильма: {}", mark);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("userId", userId, Types.BIGINT);

        if (!exists(GET_LIKE_ID_QUERY, parameterSource)) {
            log.debug("Лайк будет добавлен в БД");

            parameterSource.addValue("mark", mark, Types.REAL);

            boolean isInserted = insertWithOutReturnId(INSERT_LIKE_QUERY, parameterSource);
            if (!isInserted) {
                throw new RuntimeException(
                        "Не удалось сохранить оценку " + mark + " от пользователя с id " + userId + " фильму с id "
                                + filmId);
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
                .addValue("filmId", filmId)
                .addValue("userId", userId);

        if (exists(GET_LIKE_ID_QUERY, parameterSource)) {
            log.debug("Лайк будет удалён из БД");

            parameterSource = new MapSqlParameterSource()
                    .addValue("filmId", filmId)
                    .addValue("userId", userId);

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
        MapSqlParameterSource parameterSource = new MapSqlParameterSource().addValue("filmId", filmId)
                .addValue("genreId", genreId);

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
    public void addDirector(Long filmId, Long directorId) {
        log.debug("Добавление режиссера фильму на уровне хранилища");
        log.debug("Передан идентификатор фильма: {}", filmId == null ? "null" : filmId);
        log.debug("Передан идентификатор режиссера: {}", directorId == null ? "null" : directorId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("directorId", directorId, Types.BIGINT);

        if (!exists(GET_DIRECTOR_AND_FILM_LINK_QUERY, parameterSource)) {
            log.debug("Режиссер будет добавлен фильму");

            boolean isInserted = insertWithOutReturnId(INSERT_DIRECTOR_TO_FILM_QUERY, parameterSource);
            if (!isInserted) {
                throw new RuntimeException(
                        "Не удалось добавить режиссера с id " + directorId + " фильму с id " + filmId);
            } else {
                log.debug("Фильму с id {} добавлен режиссер с id {}", filmId, directorId);
            }
        } else {
            log.debug("Режиссер не будет добавлен фильму т.к. уже существует");
        }

        log.debug("Возврат результатов добавления режиссера на уровень сервиса");
    }

    @Override
    public void removeGenre(Long filmId, Long genreId) {
        log.debug("Удаление жанра из фильма на уровне хранилища");
        log.debug("Переданный идентификатор изменяемого фильма: {}", filmId);
        log.debug("Переданный идентификатор удаляемого жанра: {}", genreId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("genreId", genreId);

        if (exists(GET_GENRE_AND_FILM_LINK_QUERY, parameterSource)) {
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
    public void removeDirector(Long filmId, Long directorId) {
        log.debug("Удаление режиссера из фильма на уровне хранилища");
        log.debug("Передан id  фильма: {}", filmId);
        log.debug("Передан id  режиссера: {}", directorId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("directorId", directorId, Types.BIGINT);

        if (exists(GET_DIRECTOR_AND_FILM_LINK_QUERY, parameterSource)) {
            log.debug("Режиссер будет удален из фильма");

            long deletedRows = deleteOne(DELETE_DIRECTOR_ON_FILM_QUERY, parameterSource);

            if (deletedRows == 0) {
                throw new RuntimeException(
                        "Не удалось удалить связь режиссера с id " + directorId + " и фильма с id " + filmId);
            } else {
                log.debug("Режиссер с id {} больше не принадлежит фильму с id {}", directorId, filmId);
            }
        } else {
            log.debug("Режиссер не будет удален из фильма, т.к. отсутствует в БД");
        }

        log.debug("Возврат результатов удаления связи с режиссером на уровне сервиса");
    }

    @Override
    public void removeRating(Long filmId) {
        log.debug("Удаление рейтинга с фильма на уровне хранилища");
        log.debug("Передан id изменяемого фильма: {}", filmId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId);

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
                .addValue("filmId", filmId);

        long deletedRows = deleteOne(DELETE_FILM_BY_ID_QUERY, parameterSource);
        log.debug("На уровне хранилища удалено {} запись(ей)", deletedRows);
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

        // Распространяем коллекцию режиссеров
        propagateDirectors(film);

        log.debug("Распространение коллекций фильма завершено");
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
            log.debug("Добавление списка жанров завершено");
        } else {
            log.debug("Передана пустая коллекция жанров. Будет произведена очистка связей на уровне БД");
            clearGenres(film);
        }
    }

    private void propagateDirectors(Film film) {
        if (!film.getDirectors().isEmpty()) {
            log.debug("Найдена коллекция режиссеров");
            clearDirectors(film);

            log.debug("Добавление нового списка режиссеров");
            for (Long directorId : film.getDirectors()) {
                addDirector(film.getId(), directorId);
            }
            log.debug("Добавление списка режиссеров завершено");
        } else {
            log.debug("Передана пустая коллекция режиссеров. Будет произведена очистка связей на уровне БД");
            clearDirectors(film);
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

    private void clearDirectors(Film film) {
        log.debug("Очистка имеющихся режиссеров");

        Collection<Long> directorIds = directorStorage.findByFilmId(film.getId()).stream().map(Director::getId)
                .toList();

        if (!directorIds.isEmpty()) {
            log.debug("В БД найдены связи с режиссерами, подлежащие удалению");
            for (Long directorId : directorIds) {
                removeDirector(film.getId(), directorId);
            }
        }
        log.debug("Очистка коллекции режиссеров завершена");
    }

    /**
     * Метод добавляет поле в порядок сортировки результатов запроса
     *
     * @param orderBy исходная последовательность сортировки
     * @param field добавляемое к исходной сортировке поле
     * @return обработанная последовательность сортировки
     */
    private String addField(String orderBy, String field) {
        if (orderBy == null || orderBy.isBlank()) {
            orderBy = getOrderName(field);
        } else {
            orderBy += ", " + getOrderName(field);
        }

        return orderBy;
    }

    /**
     * Метод интерпретирует переданную команду в поле с указанным порядком сортировки
     *
     * @param field команда
     * @return поле с указанным порядком сортировки
     */
    private String getOrderName(String field) {
        return switch (field) {
            case "YEAR" -> "YEAR(f.RELEASE_DATE) ASC";
            case "LIKES" -> "LIKES DESC";
            case "RATE" -> "RATE DESC";
            default -> throw new RuntimeException(
                    "Для сортировки результатов запроса выбрано неизвестное имя поля " + field);
        };
    }

    /**
     * Метод добавляет переданные поля в условия поиска
     *
     * @param fields поля для поиска подстроки
     * @param query подстрока поиска
     * @return массив условий поиска
     */
    private ArrayList<String> getClauses(String[] fields, String query) {
        ArrayList<String> result = new ArrayList<>();
        for (String field : fields) {
            result.add(getClause(field.toUpperCase(), query));
        }

        return result;
    }

    /**
     * Метод создаёт условие поиска на основе переданного синонима поля и подстроки
     *
     * @param field синоним поля
     * @param query подстрока поиска
     * @return условие поиска
     */
    private String getClause(String field, String query) {
        return switch (field) {
            case "TITLE" -> "(UPPER(f.FULL_NAME) LIKE '%" + query.toUpperCase() + "%')";
            case "DIRECTOR" -> "(UPPER(d.FULL_NAME) LIKE '%" + query.toUpperCase() + "%')";
            default -> throw new RuntimeException("Для поиска подстроки указано неизвестное имя поля " + field);
        };
    }
}
