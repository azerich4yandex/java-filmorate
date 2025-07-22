package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

/**
 * Контроллер обработки HTTP-запросов для /reviews
 */
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reviews")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Обработка GET-запроса для /reviews/{id}
     *
     * @param reviewId идентификатор отзыва
     * @return экземпляр класса {@link ReviewDto}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> findById(@PathVariable(name = "id") Long reviewId) {
        log.info("Поиск отзыва по идентификатору на уровне контроллера");
        log.debug("Передан id: {}", reviewId == null ? "null" : reviewId);

        ReviewDto result = reviewService.findById(reviewId);
        log.debug("На уровень контроллера вернулся отзыв с id {}", result.getReviewId());

        log.info("Возврат результата на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /reviews?count={count}&filmId={filmId}
     *
     * @param count максимальное количество отзывов в коллекции
     * @param filmId идентификатор фильма
     * @return коллекция {@link ReviewDto}
     */
    @GetMapping
    public ResponseEntity<Collection<ReviewDto>> findByFilmId(@RequestParam(name = "filmId", required = false) Long filmId,
                                                              @RequestParam(name = "count", defaultValue = "10") Integer count) {
        log.info("Поиск отзывов по идентификатору фильма на уровне контроллера");
        log.debug("Передан максимальный размер коллекции: {}", count == null ? "null" : count);
        log.debug("Передан id фильма: {}", filmId == null ? "null" : filmId);

        Collection<ReviewDto> result;
        if (filmId != null) {
            result = reviewService.findByFilmId(filmId, count);
        } else {
            result = reviewService.findAll(count, 0);
        }
        log.debug("На уровень контроллера вернулась коллекция отзывов размером {}", result.size());

        log.debug("Возврат результатов поиска отзывов по идентификатору фильма на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка POST-запроса для /reviews
     *
     * @param request сущность {@link NewReviewRequest} из тела запроса
     * @return {@link ReviewDto} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<ReviewDto> create(@RequestBody NewReviewRequest request) {
        log.info("Запрошено создание отзыва на уровне контроллера");

        ReviewDto result = reviewService.create(request);
        log.debug("На уровень контроллера после добавления успешно вернулся отзыв с id {}", result.getReviewId());

        log.info("Возврат результата создания на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса для /reviews
     *
     * @param request несохраненный отзыв с изменениями
     * @return отзыв с сохраненными изменениями
     */
    @PutMapping
    public ResponseEntity<ReviewDto> update(@RequestBody UpdateReviewRequest request) {
        log.info("Запрошено изменение отзыва на уровне контроллера");
        log.debug("Передан для обновления отзыв с id {}", request.getReviewId());

        ReviewDto result = reviewService.update(request);
        log.debug("На уровень контроллера после изменения вернулся отзыв с id {}", result.getReviewId());

        log.info("Возврат результатов изменения на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка PUT-запроса для /reviews/{id}/like/{userId}
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable(name = "id") Long reviewId,
                                        @PathVariable(name = "userId") Long userId) {
        log.info("Запрошено добавление лайка от пользователя отзыву");
        log.debug("Передан id отзыва: {}", reviewId);
        log.debug("Передан id пользователя: {}", userId);

        reviewService.addLike(reviewId, userId);

        log.info("Возврат результатов добавления лайка от пользователя отзыву на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка PUT-запроса для /reviews/{id}/dislike/{userId}
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable(name = "id") Long reviewId,
                                           @PathVariable(name = "userId") Long userId) {
        log.info("Запрошено добавление дизлайка от пользователя отзыву");
        log.debug("Передан идентификатор отзыва {}", reviewId);
        log.debug("Передан идентификатор пользователя {}", userId);

        reviewService.addDislike(reviewId, userId);

        log.info("Возврат результатов добавления дизлайка на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /reviews/{id}/like/{userId}
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable(name = "id") Long reviewId,
                                           @PathVariable(name = "userId") Long userId) {
        log.info("Запрошено удаление лайка от пользователя отзыву");
        log.debug("Передан id  отзыва: {}", reviewId);
        log.debug("Передан id  пользователя: {}", userId);

        reviewService.removeLike(reviewId, userId);

        log.info("Возврат результатов удаления лайка от пользователя отзыву на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /reviews/{id}/dislike/{userId}
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> deleteDislike(@PathVariable(name = "id") Long reviewId,
                                              @PathVariable(name = "userId") Long userId) {
        log.info("Запрошено удаление дизлайка от пользователя отзыву");
        log.debug("Передан идентификатор  отзыва: {}", reviewId);
        log.debug("Передан идентификатор  пользователя: {}", userId);

        reviewService.removeDislike(reviewId, userId);

        log.info("Возврат результатов удаления дизлайка от пользователя отзыву на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /reviews/{id}
     *
     * @param reviewId идентификатор отзыва
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable(name = "id") Long reviewId) {
        log.info("Запрошено удаление отзыва на уровне контроллера");
        log.debug("Передан  id  отзыва: {}", reviewId);

        reviewService.deleteReview(reviewId);

        log.info("Возврат результатов удаления отзыва на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /reviews
     */
    @DeleteMapping
    public ResponseEntity<Void> clearReviews() {
        log.info("Запрошена очистка хранилища отзывов на уровне контроллера");

        reviewService.clearReviews();

        log.info("Возврат результатов очистки хранилища отзывов на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
