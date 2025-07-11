package ru.yandex.practicum.filmorate.dal.mpa;

import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseDbStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

@Slf4j
@Component
public class MpaDbStorage extends BaseDbStorage<Mpa> implements MpaStorage {

    private static final String GET_ALL_RATINGS_QUERY = """
            SELECT r.ID,
                   r.FULL_NAME
              FROM RATINGS r
             ORDER BY r.ID
             LIMIT ?
            OFFSET ?
            """;
    private static final String GET_RATING_BY_ID_QUERY = """
            SELECT r.ID,
                   r.FULL_NAME
              FROM RATINGS r
             WHERE r.ID = ?
            """;
    private static final String INSERT_RATING_QUERY = """
            INSERT INTO RATING (FULL_NAME)
            VALUES (?)
            """;
    private static final String UPDATE_RATING_QUERY = """
            UPDATE RATINGS
               SET FULL_NAME = ?
             WHERE ID = ?
            """;
    private static final String DELETE_RATING_BY_ID_QUERY = """
            DELETE FROM RATINGS
             WHERE ID = ?
            """;
    private static final String DELETE_ALL_RATINGS_QUERY = """
            DELETE FROM RATINGS
            """;
    private static final String GET_RAT_NAME_BEFORE_UPDATE_QUERY = """
            SELECT r.ID
              FROM RATINGS r
             WHERE UPPER(r.FULL_NAME) = ?
               AND r.ID != ?
            """;
    private static final String GET_RAT_NAME_BEFORE_INSERT_QUERY = """
            SELECT r.ID
              FROM RATINGS r
             WHERE UPPER(r.FULL_NAME) = ?
            """;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate, MpaRowMapper mapper, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(jdbcTemplate, namedParameterJdbcTemplate, mapper);
    }

    @Override
    public Collection<Mpa> findAll(Integer size, Integer from) {
        log.debug("Запрос всех рейтингов на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Mpa> result = findMany(GET_ALL_RATINGS_QUERY, size, from);
        log.debug("Получена коллекция всех рейтингов размеров {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Mpa> findById(Long ratingId) {
        log.debug("Поиск рейтинга по id на уровне хранилища");
        log.debug("Идентификатор запрашиваемого рейтинга: {}", ratingId);

        Optional<Mpa> result = findOne(GET_RATING_BY_ID_QUERY, ratingId);

        log.debug("Возврат результата поиска на уровень сервиса");
        return result;
    }

    @Override
    public Mpa createRating(Mpa rating) {
        log.debug("Создание рейтинга на уровне хранилища");

        long id = insert(INSERT_RATING_QUERY, rating.getName());
        if (id == 0) {
            throw new RuntimeException("Не удалось добавить рейтинг в БД");
        }
        log.debug("Сгенерировано значение {}", id);

        rating.setId(id);
        log.debug("Значение присвоено id присвоено рейтингу");

        log.debug("Возврат результатов создания на уровень сервиса");
        return rating;
    }

    @Override
    public Mpa updateRating(Mpa newRating) {
        log.debug("Изменение рейтинга на уровне хранилища");
        long updatedRows = update(UPDATE_RATING_QUERY, newRating.getName(), newRating.getId());
        log.debug("На уровне хранилища обновлено {} запись(ей)", updatedRows);

        log.debug("Возврат результатов изменения на уровень сервиса");
        return newRating;
    }

    @Override
    public void deleteRating(Long ratingId) {
        log.debug("Удаление жанра на уровне хранилища");
        log.debug("Передан id жанра: {}", ratingId);

        long deletedRows = deleteOne(DELETE_RATING_BY_ID_QUERY, ratingId);

        if (deletedRows == 0) {
            throw new RuntimeException("Не удалось удалить рейтинг с id " + ratingId);
        }

        log.debug("Жанр с id {} удален из хранилища", ratingId);

        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearRatings() {
        log.debug("Очистка хранилища жанров");

        deleteMany(DELETE_ALL_RATINGS_QUERY);

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    @Override
    public boolean isNameAlreadyUser(Mpa rating) {
        log.debug("Проверка на использование наименования жанра {} другими жанрами", rating.getName());

        if (rating.getId() != null) {
            return exists(GET_RAT_NAME_BEFORE_UPDATE_QUERY, rating.getName().toUpperCase(), rating.getId());
        } else {
            return exists(GET_RAT_NAME_BEFORE_INSERT_QUERY, rating.getName().toUpperCase());
        }
    }
}