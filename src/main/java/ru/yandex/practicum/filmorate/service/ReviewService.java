package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.review.ReviewStorage;
import ru.yandex.practicum.filmorate.dal.user.UserStorage;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    /**
     * Метод возвращает коллекцию {@link ReviewDto}
     *
     * @param size максимальный размер коллекции
     * @param from номер стартового элемента
     * @return результирующая коллекция
     */
    public Collection<ReviewDto> findAll(Integer size, Integer from) {
        log.debug("Запрос всех отзывов на фильм на уровне сервиса");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Review> searchResult = reviewStorage.findAll(size, from);
        log.debug("На уровень сервиса вернулась коллекция размером {}", searchResult.size());

        Collection<ReviewDto> result = searchResult.stream().map(ReviewMapper::mapToReviewDto).toList();
        log.debug("Найденная коллекция преобразована. Размер коллекции после преобразования: {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает коллекцию {@link ReviewDto}, связанных с конкретным фильмом
     *
     * @param filmId идентификатор конкретного фильма
     * @param count размер результирующей коллекции
     * @return результирующая коллекция
     */
    public Collection<ReviewDto> findByFilmId(Long filmId, Integer count) {
        log.debug("Поиск всех отзывов на фильм на уровне сервиса");
        log.debug("Переданный идентификатор фильма: {}", filmId);

        // Проверяем размер запрашиваемой коллекции
        if (count == null) {
            throw new ValidationException("Не указан размер запрашиваемой коллекции");
        } else if (count <= 0) {
            throw new ValidationException("Размер запрашиваемой коллекции должен быть больше 0");
        }

        // Проверяем наличие фильма в хранилище
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден в хранилище"));

        Collection<Review> searchResult = reviewStorage.findByFilmId(film.getId(), count);
        log.debug("На уровень сервиса вернулась коллекция отзывов к конкретному фильму размером {}",
                searchResult.size());

        Collection<ReviewDto> result = searchResult.stream().map(ReviewMapper::mapToReviewDto).toList();
        log.debug(
                "Найденная коллекция отзывов к конкретному фильму преобразована. размер преобразованной коллекции: {}",
                result.size());

        log.debug("Возврат результатов поиска всех отзывов к конкретному фильму на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link ReviewDto}, найденный по идентификатору
     * @param reviewId идентификатор отзыва
     * @return экземпляр класса {@link ReviewDto}
     */
    public ReviewDto findById(Long reviewId) {
        log.debug("Поиск отзыва по идентификатору на уровне сервиса");
        log.debug("Переданный идентификатор отзыва: {}", reviewId);

        Review searchResult = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден в хранилище"));

        ReviewDto result = ReviewMapper.mapToReviewDto(searchResult);
        log.debug("Найденный отзыв с id {} преобразован", result.getReviewId());

        log.debug("Возврат результата поиска отзыва по идентификатору на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для сохранения на уровень хранилища, после чего сохраненную модель возвращает на уровень контроллера
     *
     * @param request несохраненный экземпляр {@link NewReviewRequest}
     * @return сохраненный экземпляр {@link ReviewDto}
     */
    public ReviewDto create(NewReviewRequest request) {
        log.debug("Создание отзыва на уровне сервиса");

        Review review = ReviewMapper.mapToReview(request);

        log.debug("Валидация переданной модели");
        validate(review);
        log.debug("Валидация модели завершена");

        review = reviewStorage.createReview(review);

        ReviewDto result = ReviewMapper.mapToReviewDto(review);
        log.debug("Сохранённая модель преобразована");

        log.debug("Возврат результатов добавления на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для обновления на уровень хранилища, после чего сохраненную модель возвращает на уровень контроллера
     *
     * @param request несохраненный экземпляр класса {@link UpdateReviewRequest}
     * @return обновленный экземпляр класса {@link ReviewDto}
     */
    public ReviewDto update(UpdateReviewRequest request) {
        log.debug("Обновление отзыва на уровне хранилища");

        if (request.getReviewId() == null) {
            throw new ValidationException("Id должен быт указан");
        }

        Review existingReview = reviewStorage.findById(request.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + request.getReviewId() + " не найден"));
        log.debug("Отзыв с id {} найден в хранилище для обновления", request.getReviewId());

        Review updatedReview = ReviewMapper.updateReviewFields(existingReview, request);

        log.debug("Валидация обновленной модели");
        validate(updatedReview);
        log.debug("Валидация обновленной модели завершена");

        // Сохраняем изменения
        reviewStorage.updateReview(updatedReview);

        ReviewDto result = ReviewMapper.mapToReviewDto(updatedReview);
        log.debug("Обновленная модель преобразована");

        log.debug("Возврат результата обновления на уровень контроллера");
        return result;

    }

    /**
     * Метод добавляет лайк отзыву от пользователя
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    public void addLike(Long reviewId, Long userId) {
        log.debug("Добавление лайка отзыву на уровне сервиса");
        log.debug("Передан идентификатор отзыва: {}", reviewId);
        log.debug("Передан идентификатор пользователя: {}", userId);

        if (reviewId == null) {
            throw new ValidationException("Передан пустой id отзыва");
        }

        if (userId == null) {
            throw new ValidationException("Передан пустой id пользователя");
        }

        // Получаем отзыв из хранилища
        Review review = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден в хранилище"));

        // Получаем пользователя из хранилища
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Добавляем лайк
        reviewStorage.addReviewLike(review.getId(), user.getId());
        log.debug("Лайк отзыву от пользователя добавлен на уровне сервиса");

        log.debug("Возврат результата добавления лайка на уровень контроллера");
    }

    /**
     * Метод удаляет лайк с отзыва от пользователя
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    public void removeLike(Long reviewId, Long userId) {
        log.debug("Удаление лайка от пользователя с отзыва");
        log.debug("Передан id отзыва: {}", reviewId);
        log.debug("Передан id пользователя: {}", userId);

        if (reviewId == null) {
            throw new ValidationException("Передан пустой id отзыва");
        }

        if (userId == null) {
            throw new ValidationException("Передан пустой id пользователя");
        }

        // Получаем отзыв из хранилища
        Review review = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден в хранилище"));

        // Получаем пользователя из хранилища
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Удаляем лайк
        reviewStorage.removeReviewLike(review.getId(), user.getId());
        log.debug("Лайк от пользователя удален с отзыва на уровне сервиса");

        log.debug("Возврат результатов удаления лайка на уровень сервиса");
    }

    /**
     * Метод добавляет дизлайк отзыву от пользователя
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    public void addDislike(Long reviewId, Long userId) {
        log.debug("Добавление дизлайка от пользователя с отзыва");
        log.debug("Передан id  отзыва: {}", reviewId);
        log.debug("Передан id  пользователя: {}", userId);

        if (reviewId == null) {
            throw new ValidationException("Передан пустой id отзыва");
        }

        if (userId == null) {
            throw new ValidationException("Передан пустой id пользователя");
        }

        // Получаем отзыв из хранилища
        Review review = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден в хранилище"));

        // Получаем пользователя из хранилища
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Добавляем дизлайк
        reviewStorage.addReviewDislike(review.getId(), user.getId());
        log.debug("Дизлайк от пользователя добавлен к отзыву на уровне сервиса");

        log.debug("Возврат результатов добавления дизлайка на уровень контроллера");
    }

    /**
     * Метод удаляет дизлайк от пользователя с отзыва
     *
     * @param reviewId идентификатор отзыва
     * @param userId идентификатор пользователя
     */
    public void removeDislike(Long reviewId, Long userId) {
        log.debug("Удаление дизлайка от пользователя с отзыва");
        log.debug("Передан  id отзыва: {}", reviewId);
        log.debug("Передан  id пользователя: {}", userId);

        if (reviewId == null) {
            throw new ValidationException("Передан пустой id отзыва");
        }

        if (userId == null) {
            throw new ValidationException("Передан пустой id пользователя");
        }

        // Получаем отзыв из хранилища
        Review review = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден в хранилище"));

        // Получаем пользователя из хранилища
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Удаляем дизлайк
        reviewStorage.removeReviewDislike(review.getId(), user.getId());
        log.debug("Дизлайк от пользователя удален с отзыва на уровне сервиса");

        log.debug("Возврат результатов удаления дизлайка на уровень контролера");
    }

    /**
     * Метод удаляет отзыв из хранилища
     *
     * @param reviewId идентификатор отзыва
     */
    public void deleteReview(Long reviewId) {
        log.debug("Удаление отзыва");
        log.debug("Передан  идентификатор отзыва: {}", reviewId);

        if (reviewId == null) {
            throw new ValidationException("Передан пустой id отзыва");
        }

        // Получаем отзыв из хранилища
        Review review = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + reviewId + " не найден в хранилище"));

        // Удаляем отзыва
        reviewStorage.deleteReview(review.getId());
        log.debug("Отзыв удален на уровне сервиса");

        log.debug("Возврат результатов удаления отзыва на уровень контроллера");
    }

    /**
     * Метод очищает хранилище отзывов
     */
    public void clearReviews() {
        log.debug("Удаление всех отзывов на уровне хранилища");

        // Очищаем хранилище
        reviewStorage.clearReviews();
        log.debug("Все отзывы удалены");

        log.debug("Возврат результатов очистки на уровень контроллера");
    }

    /**
     * Валидация сущности {@link Review} на правильное заполнение ключевых полей
     *
     * @param review экземпляр класса {@link Review}
     */
    private void validate(Review review) {
        // Валидация содержимого
        validateContent(review.getContent());

        // Валидация пользователя
        validateUserId(review.getUserId());

        // Валидация фильма
        validateFilmId(review.getFilmId());
    }

    /**
     * Валидация содержимого сущности {@link Review}
     *
     * @param content содержимое отзыва
     */
    private void validateContent(String content) {
        log.debug("Запускам валидацию содержимого обзора");
        if (content == null || content.isBlank()) {
            throw new ValidationException("Содержимое обзора должно быть указано");
        }

        log.debug("Передано корректное значение content: {}", content);
        log.debug("Валидация содержимого успешно завершена");
    }

    /**
     * Валидация связанного пользователя сущности {@link Review}
     *
     * @param userId идентификатор пользователя
     */
    private void validateUserId(Long userId) {
        log.debug("Запускаем валидацию переданного идентификатора пользователя");

        if (userId == null) {
            throw new ValidationException("Id пользователя должен быть указан");
        }

        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        log.debug("Передано корректное значение userId: {}", user.getId());
        log.debug("Валидация пользователя успешно завершена");
    }

    /**
     * Валидация связанного фильма сущности {@link Review}
     *
     * @param filmId идентификатор фильма
     */
    private void validateFilmId(Long filmId) {
        log.debug("Запускаем валидацию переданного идентификатора фильма");

        if (filmId == null) {
            throw new ValidationException("Id фильма должен быть указан");
        }

        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден в хранилище"));

        log.debug("Передано корректное значение filmId: {}", film.getId());
        log.debug("Валидация фильма успешно завершена");
    }
}
