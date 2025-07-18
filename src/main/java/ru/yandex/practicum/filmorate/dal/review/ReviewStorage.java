package ru.yandex.practicum.filmorate.dal.review;

import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;

/**
 * Интерфейс обработки сущностей {@link Review} на уровне хранилища
 */
@Service
public interface ReviewStorage {

    /**
     * Метод возвращает коллекцию сущностей {@link Review} на уровне хранилища
     *
     * @param size максимальный размер возвращаемой коллекции
     * @param from номер стартового элемента
     * @return коллекция {@link Review}
     */
    Collection<Review> findAll(Integer size, Integer from);

    /**
     * Метод возвращает коллекцию отзывов к фильму
     *
     * @param filmId идентификатор фильма
     * @param count размер результирующей коллекции
     * @return коллекция {@link Review}
     */
    Collection<Review> findByFilmId(Long filmId, Integer count);

    /**
     * Метод возвращает экземпляр сущности {@link Review} по переданному идентификатору
     *
     * @param reviewId идентификатор сущности
     * @return экземпляр {@link Review}
     */
    Optional<Review> findById(Long reviewId);

    /**
     * Метод создаёт в хранилище переданный экземпляр класса {@link Review}
     *
     * @param review экземпляр класса {@link Review}
     * @return экземпляр класса {@link Review} с заполненными автоматически генерируемыми полями из хранилища
     */
    Review createReview(Review review);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link Review}
     *
     * @param newReview экземпляр класса {@link Review} для обновления
     */
    void updateReview(Review newReview);

    /**
     * Метод добавляет лайк отзыву от пользователя
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    void addReviewLike(Long reviewId, Long userId);


    /**
     * Метод удаляет лайк с отзыва от пользователя
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    void removeReviewLike(Long reviewId, Long userId);

    /**
     * Метод добавляет дизлайк отзыву от пользователя
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    void addReviewDislike(Long reviewId, Long userId);

    /**
     * Метод удаляет дизлайк отзыва от пользователя
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    void removeReviewDislike(Long reviewId, Long userId);

    /**
     * Метод удаляет отзыв по идентификатору
     *
     * @param reviewId идентификатор отзыва
     */
    void deleteReview(Long reviewId);

    /**
     * Метод очищает хранилище от отзывов
     */
    void clearReviews();
}
