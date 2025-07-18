package ru.yandex.practicum.filmorate.dal.review;

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
import ru.yandex.practicum.filmorate.model.Review;

@Slf4j
@Component
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    private static final String GET_ALL_REVIEWS_QUERY = """
            SELECT r.ID,
                   r.USER_ID,
                   r.FILM_ID,
                   r.CONTENT,
                   r.IS_POSITIVE,
                   NVL(SUM(ur.USEFUL), 0) AS useful
              FROM REVIEWS r
              LEFT JOIN USERS_REVIEWS ur ON r.ID = ur.REVIEW_ID
             GROUP BY r.ID,
                      r.USER_ID,
                      r.FILM_ID,
                      r.CONTENT,
                      r.IS_POSITIVE
             ORDER BY useful DESC, r.ID DESC
             LIMIT :size
            OFFSET :from
            """;
    private static final String GET_ALL_REVIEWS_BY_FILM_ID_QUERY = """
            SELECT r.ID,
                   r.USER_ID,
                   r.FILM_ID,
                   r.CONTENT,
                   r.IS_POSITIVE,
                   NVL(SUM(ur.USEFUL), 0) AS useful
              FROM REVIEWS r
              LEFT JOIN USERS_REVIEWS ur ON r.ID = ur.REVIEW_ID
             WHERE r.FILM_ID = :filmId
             GROUP BY r.ID,
                      r.USER_ID,
                      r.FILM_ID,
                      r.CONTENT,
                      r.IS_POSITIVE
             ORDER BY useful DESC, r.ID DESC
             LIMIT :count
            """;
    private static final String GET_REVIEW_BY_ID_QUERY = """
            SELECT r.ID,
                   r.USER_ID,
                   r.FILM_ID,
                   r.CONTENT,
                   r.IS_POSITIVE,
                   NVL(SUM(ur.USEFUL), 0) AS useful
              FROM REVIEWS r
              LEFT JOIN USERS_REVIEWS ur ON r.ID = ur.REVIEW_ID
             WHERE r.ID = :reviewId
             GROUP BY r.ID,
                      r.USER_ID,
                      r.FILM_ID,
                      r.CONTENT,
                      r.IS_POSITIVE
            """;
    private static final String INSERT_REVIEW_QUERY = """
            INSERT INTO REVIEWS(USER_ID, FILM_ID, CONTENT, IS_POSITIVE)
            VALUES (:userId, :filmId, :content, :isPositive)
            """;
    private static final String UPDATE_REVIEW_QUERY = """
            UPDATE REVIEWS
               SET USER_ID = :userId,
                   FILM_ID = :filmId,
                   CONTENT = :content,
                   IS_POSITIVE = :isPositive
             WHERE ID = :reviewId
            """;
    private static final String INSERT_REVIEW_LIKE_QUERY = """
            INSERT INTO USERS_REVIEWS (USER_ID, REVIEW_ID, USEFUL)
            VALUES (:userId, :reviewId, :useful)
            """;
    private static final String DELETE_REVIEW_LIKE_QUERY = """
            DELETE FROM USERS_REVIEWS
             WHERE USER_ID = :userId
               AND REVIEW_ID = :reviewId
            """;
    private static final String DELETE_REVIEW_BY_ID_QUERY = """
            DELETE FROM REVIEWS
             WHERE ID = :reviewId
            """;
    private static final String DELETE_ALL_REVIEWS_QUERY = """
            DELETE FROM REVIEWS
            """;

    @Autowired
    public ReviewDbStorage(NamedParameterJdbcTemplate jdbcTemplate,
                           RowMapper<Review> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Collection<Review> findAll(Integer size, Integer from) {
        log.debug("Запрос всех отзывов на уровне хранилища");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("size", size, Types.BIGINT)
                .addValue("from", from, Types.BIGINT);

        Collection<Review> result = findMany(GET_ALL_REVIEWS_QUERY, parameterSource);
        log.debug("Получена коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Review> findByFilmId(Long filmId, Integer count) {
        log.debug("Запрос всех отзывов по идентификатору фильма на уровне хранилища");
        log.debug("Идентификатор фильма: {}", filmId);
        log.debug("Максимальный размер коллекции: {}", count);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId, Types.BIGINT)
                .addValue("count", count, Types.BIGINT);

        Collection<Review> result = findMany(GET_ALL_REVIEWS_BY_FILM_ID_QUERY, parameterSource);
        log.debug("Получена коллекция отзывов размером {}", result.size());

        log.debug("Возврат результатов на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        log.debug("Запрос экземпляра по идентификатору на уровне хранилища");
        log.debug("Передан идентификатор отзыва: {}", reviewId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("reviewId", reviewId, Types.BIGINT);

        Optional<Review> result = findOne(GET_REVIEW_BY_ID_QUERY, parameterSource);

        if (result.isPresent()) {
            log.debug("Возврат результата поиска на уровень сервиса");
            return result;
        } else {
            log.debug("Возврат пустого результата на уровень сервиса");
            return Optional.empty();
        }
    }

    @Override
    public Review createReview(Review review) {
        log.debug("Создание экземпляра отзыва на уровне хранилища");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", review.getUserId(), Types.BIGINT)
                .addValue("filmId", review.getFilmId(), Types.BIGINT)
                .addValue("content", review.getContent(), Types.NVARCHAR)
                .addValue("isPositive", review.isPositive(), Types.BOOLEAN);

        long id = insert(INSERT_REVIEW_QUERY, parameterSource);

        if (id == 0) {
            throw new RuntimeException("Не удалось сохранить отзыв в БД");
        } else {
            log.debug("Сгенерировано значение id: {}", id);
        }

        review.setId(id);

        log.debug("Возврат результатов создания на уровень сервиса");
        return review;
    }

    @Override
    public void updateReview(Review newReview) {
        log.debug("Запрос обновления отзыва на уровне хранилища");
        log.debug("Передан идентификатор отзыва {}", newReview.getId());

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", newReview.getUserId(), Types.BIGINT)
                .addValue("filmId", newReview.getFilmId(), Types.BIGINT)
                .addValue("content", newReview.getContent(), Types.NVARCHAR)
                .addValue("isPositive", newReview.isPositive(), Types.BOOLEAN)
                .addValue("reviewId", newReview.getId(), Types.BIGINT);

        long updatedRows = update(UPDATE_REVIEW_QUERY, parameterSource);

        if (updatedRows == 0) {
            throw new RuntimeException("Не удалось обновить отзыв с id " + newReview.getId());
        } else {
            log.debug("На уровне хранилища обновлено {} запись(ей)", updatedRows);
        }

        log.debug("Возврат результатов обновления на уровень сервиса");
    }

    @Override
    public void addReviewLike(Long reviewId, Long userId) {
        log.debug("Добавление лайка отзыву на уровне хранилища");
        log.debug("Передан id отзыва: {}", reviewId);
        log.debug("Передан id пользователя: {}", userId);

        removeReviewUseful(reviewId, userId);
        if (addReviewUseful(reviewId, userId, 1)) {
            log.debug("Добавление лайка отзыву с id {} от пользователя с id {} успешно произведено", reviewId, userId);
        } else {
            throw new RuntimeException(
                    "Не удалось добавить лайк отзыву с id " + reviewId + " от пользователя с id " + userId);
        }

        log.debug("Возврат результата добавления лайка отзыву на уровень сервиса");
    }

    @Override
    public void removeReviewLike(Long reviewId, Long userId) {
        log.debug("Удаление лайка с отзыва на уровне хранилища");
        log.debug("Передан id  отзыва: {}", reviewId);
        log.debug("Передан id  пользователя: {}", userId);

        removeReviewUseful(reviewId, userId);

        log.debug("Возврат результата удаления лайка отзыву на уровень сервиса");
    }

    @Override
    public void addReviewDislike(Long reviewId, Long userId) {
        log.debug("Добавление дизлайка отзыву на уровне хранилища");
        log.debug("Передан  id отзыва: {}", reviewId);
        log.debug("Передан  id пользователя: {}", userId);

        removeReviewUseful(reviewId, userId);

        if (addReviewUseful(reviewId, userId, -1)) {
            log.debug("Добавление дизлайка отзыву с id {} от пользователя с id {} успешно произведено", reviewId, userId);
        } else {
            throw new RuntimeException(
                    "Не удалось добавить дизлайк отзыву с id " + reviewId + " от пользователя с id " + userId);
        }

        log.debug("Возврат результата добавления дизлайка отзыву на уровень сервиса");
    }

    @Override
    public void removeReviewDislike(Long reviewId, Long userId) {
        log.debug("Удаление дизлайка с отзыва на уровне хранилища");
        log.debug("Передан  id  отзыва: {}", reviewId);
        log.debug("Передан  id  пользователя: {}", userId);

        removeReviewUseful(reviewId, userId);

        log.debug("Возврат результата удаления дизлайка отзыву на уровень сервиса");
    }

    @Override
    public void deleteReview(Long reviewId) {
        log.debug("Удаление отзыва на уровне хранилища");
        log.debug("Передан идентификатор  отзыва: {}", reviewId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("reviewId", reviewId, Types.BIGINT);

        long deletedRows = deleteOne(DELETE_REVIEW_BY_ID_QUERY, parameterSource);

        if (deletedRows == 0) {
            throw new RuntimeException("Не удалось удалить отзыв с id " + reviewId);
        } else {
            log.debug("Отзыв с id {} удален из хранилища", reviewId);
        }

        log.debug("Возврат результатов удаления отзыва на уровень сервиса");
    }

    @Override
    public void clearReviews() {
        log.debug("Очистка хранилища отзывов");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        long deletedRows = deleteMany(DELETE_ALL_REVIEWS_QUERY, parameterSource);
        log.debug("На уровне хранилища удалено {} запись(ей)", deletedRows);

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    private void removeReviewUseful(Long reviewId, Long userId) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("reviewId", reviewId, Types.BIGINT)
                .addValue("userId", userId, Types.BIGINT);

        deleteOne(DELETE_REVIEW_LIKE_QUERY, parameterSource);
    }

    private boolean addReviewUseful(Long reviewId, Long userId, Integer useful) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("reviewId", reviewId, Types.BIGINT)
                .addValue("userId", userId, Types.BIGINT)
                .addValue("useful", useful);

        return insertWithOutReturnId(INSERT_REVIEW_LIKE_QUERY, parameterSource);
    }
}
