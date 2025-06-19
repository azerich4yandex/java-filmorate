package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.db.dto.create.NewMpaRequest;
import ru.yandex.practicum.filmorate.storage.db.dto.read.MpaDto;
import ru.yandex.practicum.filmorate.storage.db.dto.update.UpdateMpaRequest;
import ru.yandex.practicum.filmorate.storage.db.mappers.MpaMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;
    private final FilmStorage filmStorage;

    /**
     * Метод возвращает коллекцию {@link MpaDto}
     *
     * @param size максимальный размер коллекции
     * @param from номер стартового элемента
     * @return результирующая коллекция
     */
    public Collection<MpaDto> findAll(Integer size, Integer from) {
        log.debug("Запрос всех жанров на уровне сервиса");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Mpa> searchResult = mpaStorage.findAll(size, from);
        log.debug("На уровень сервиса вернулась коллекция размером {}", searchResult.size());

        Collection<MpaDto> result = searchResult.stream().map(MpaMapper::mapToMpaDto).toList();
        log.debug("Найденная коллекция преобразована. Размер коллекции после преобразования: {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link MpaDto}, найденный по идентификатору
     *
     * @param ratingId идентификатор рейтинга
     * @return экземпляр класса {@link MpaDto}
     * @throws ValidationException если передан пустой ratingId
     * @throws NotFoundException если экземпляр не найден
     */
    public MpaDto findById(Long ratingId) throws ValidationException, NotFoundException {
        log.debug("Поиск рейтинга по id на уровне сервиса");

        if (ratingId == null) {
            throw new ValidationException("Передан пустой ratingId");
        }
        log.debug("Передан id рейтинга: {}", ratingId);

        Mpa searchResult = mpaStorage.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + ratingId + " не найден"));
        log.debug("Рейтинг с id {} найден в хранилище", searchResult.getId());

        MpaDto result = MpaMapper.mapToMpaDto(searchResult);
        log.debug("Найденный рейтинг с id {} преобразован", result.getId());

        log.debug("Возврат результата поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для сохранения на уровень хранилища, после чего сохранённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохранённый экземпляр класса {@link NewMpaRequest}
     * @return сохранённый экземпляр класса {@link Mpa}
     */
    public MpaDto create(NewMpaRequest request) {
        log.debug("Создание рейтинга на уровне сервиса");

        Mpa rating = MpaMapper.mapToMpa(request);
        log.debug("Переданная модель преобразована");

        log.debug("Валидация переданной модели");
        validate(rating);
        log.debug("Валидация модели завершена");

        rating = mpaStorage.createRating(rating);

        MpaDto result = MpaMapper.mapToMpaDto(rating);
        log.debug("Сохранённая модель преобразована");

        log.debug("Возврат результатов добавления на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для обновления на уровень хранилища, после чего сохранённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохранённый экземпляр класса {@link UpdateMpaRequest}
     * @return сохранённый экземпляр класса {@link MpaDto}
     * @throws ValidationException при ошибках валидации
     */
    public MpaDto update(UpdateMpaRequest request) throws ValidationException {
        log.debug("Обновление рейтинга на уровне сервиса");

        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Передан рейтинг с id: {}", request.getId());

        // Получаем рейтинг из хранилища
        Mpa existingRating = mpaStorage.findById(request.getId()).orElseThrow(
                () -> new NotFoundException("Рейтинг с id " + request.getId() + " не найден в хранилище"));
        log.debug("Рейтинг с id {} найден  в хранилище", existingRating.getId());

        Mpa updatedRating = MpaMapper.updateMpaFields(existingRating, request);

        // Проверяем переданный рейтинг
        log.debug("Валидация обновлённой модели");
        validate(updatedRating);
        log.debug("Валидация обновлённой модели завершена");

        // Сохраняем изменения
        mpaStorage.updateRating(updatedRating);

        MpaDto result = MpaMapper.mapToMpaDto(updatedRating);
        log.debug("Обновленная модель преобразована");

        log.debug("Возврат результатов обновления на уровень контроллера");
        return result;
    }

    /**
     * Метод удаляет экземпляр {@link Mpa} по переданному id
     *
     * @param ratingId идентификатор рейтинга
     * @throws ValidationException при ошибках валидации
     * @throws NotFoundException если удаляемый рейтинг не найден в хранилище
     */
    public void deleteRating(Long ratingId) throws ValidationException, NotFoundException {
        log.debug("Удаление рейтинга на уровне сервиса");

        if (ratingId == null) {
            throw new ValidationException("Передан пустой id");
        }
        log.debug("Передан id удаляемого рейтинга: {}", ratingId);

        // Получаем рейтинг из хранилища
        Mpa rating = mpaStorage.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + ratingId + " не найден"));
        log.debug("В хранилище найден рейтинг с id {}", rating.getId());

        log.debug("Получаем фильмы с таким же рейтингом");
        Collection<Film> films = filmStorage.findByRatingId(rating.getId());
        log.debug("На уровень сервиса вернулась коллекция фильмов размером {}", films.size());

        // Удаляем рейтинги из найденных фильмов
        if (!films.isEmpty()) {
            log.debug("Рейтинг используется другими фильмами");

            for (Film film : films) {
                // Удаляем рейтинг из фильма
                filmStorage.removeRating(film.getId());
            }
        } else {
            log.debug("Рейтинг не используется другими фильмами");
        }

        // Удаляем рейтинг
        mpaStorage.deleteRating(rating.getId());

        log.debug("Возврат результата удаления на уровень контроллера");
    }

    public void clearGenres() {
        log.debug("Очистка рейтингов на уровне сервиса");

        mpaStorage.clearRatings();

        log.debug("Возврат результатов очистки на уровень контроллера");
    }

    private void validate(Mpa rating) {
        // Валидация наименования
        validateName(rating);
    }

    private void validateName(Mpa rating) throws ValidationException {
        log.debug("Запускаем валидацию наименования");

        // Наименование не должно быть пустым
        if (rating.getName() == null || rating.getName().isBlank()) {
            throw new ValidationException("Наименование не может быть пустым");
        }

        // Наименование не должно быть ранее использовано
        boolean exists = mpaStorage.isNameAlreadyUser(rating);

        if (exists) {
            throw new ValidationException("Наименование " + rating.getName() + " уже используется");
        }

        log.debug("Передано корректное значение name: {}", rating.getName());
        log.debug("Валидация наименования завершена");
    }
}
