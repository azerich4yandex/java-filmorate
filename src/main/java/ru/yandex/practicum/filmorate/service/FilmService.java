package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.film.FilmStorage;
import ru.yandex.practicum.filmorate.dal.genre.GenreStorage;
import ru.yandex.practicum.filmorate.dal.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.dal.user.UserStorage;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.user.UserShortDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

/**
 * Класс предварительной обработки и валидации сущностей {@link User} на уровне сервиса
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    /**
     * Метод возвращает коллекцию {@link FilmDto}
     *
     * @param size максимальный размер коллекции
     * @param from номер стартового элемента
     * @return результирующая коллекция
     */
    public Collection<FilmDto> findAll(Integer size, Integer from) {
        log.debug("Запрос всех фильмов на уровне сервиса");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Film> searchResult = filmStorage.findAll(size, from);
        log.debug("На уровень сервиса вернулась коллекция размером {}", searchResult.size());

        Collection<FilmDto> result = searchResult.stream().map(FilmMapper::mapToFilmDto).toList();

        // Перебираем полученную коллекцию
        for (FilmDto film : result) {
            // Заполняем коллекции
            completeDto(film);
        }
        log.debug("Найденная коллекция преобразована. Размер коллекции после преобразования: {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    public Collection<FilmDto> findCommon(Long userId, Long friendId) {
        log.debug("Поиск общих фильмов на уровне сервиса");

        // Проверяем наличие пользователя
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        // Проверяем наличие друга
        User friend = userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        Collection<Film> searchResult = filmStorage.findCommon(user.getId(), friend.getId());
        log.debug("Получена коллекция общих фильмов размером {}", searchResult.size());

        Collection<FilmDto> result = searchResult.stream().map(FilmMapper::mapToFilmDto).toList();

        // Перебираем полученную коллекцию
        for (FilmDto film : result) {
            // Заполняем коллекции
            completeDto(film);
        }
        log.debug("Найденная коллекция общих фильмов преобразована. Размер преобразованной коллекции: {}", result.size());

        log.debug("Возврат коллекции общих фильмов на уровень контроллера");
        return result;
    }

    /**
     * Метод получает список из {@link FilmDto} на основе размера коллекции {@link FilmDto#getLikes()}
     *
     * @param count размер возвращаемой коллекции
     * @return результирующая коллекция
     * @throws ValidationException если передано пустой размер возвращаемой коллекции
     */
    public Collection<FilmDto> findPopular(Integer count, Long genreId, Integer year) throws ValidationException {
        log.debug("Поиск топ фильмов на уровне сервиса");

        // Если передано отрицательное значение
        if (count <= 0) {
            throw new ValidationException("Значение count должно быть больше нуля");
        }

        // Жанр должен существовать, если указан
        if (genreId != null) {
            Genre genre = genreStorage.findById(genreId)
                    .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + " не найден"));
            log.debug("Для поиска топ-фильмов указан жанр с id {}", genre.getId());
        } else {
            log.debug("Жанр для поиска топ-фильмов не указан");
        }

        // Год должен быть в рамках диапазона, если указан
        if (year != null) {
            if (year < 1895 || year > Year.now().getValue()) {
                throw new ValidationException("Передан некорректный год: " + year);
            }
            log.debug("Для поиска топ-фильмов указан год {}", year);
        } else {
            log.debug("Год для поиска топ-фильмов не указан");
        }

        Collection<Film> searchResult = filmStorage.findPopular(count, genreId, year);
        log.debug("Получена коллекция топ-фильмов размером {}", searchResult.size());

        Collection<FilmDto> result = searchResult.stream().map(FilmMapper::mapToFilmDto).toList();

        // Перебираем полученную коллекцию
        for (FilmDto film : result) {
            // Заполняем коллекции
            completeDto(film);
        }
        log.debug("Найденная коллекция топ-фильмов преобразована. Размер преобразованной коллекции: {}", result.size());

        log.debug("Возврат коллекции топ-фильмов на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link FilmDto}, найденный по идентификатору
     *
     * @param filmId идентификатор фильма
     * @return экземпляр класса {@link FilmDto}
     * @throws ValidationException если передан пустой filmId
     * @throws NotFoundException если экземпляр не найден
     */
    public FilmDto findById(Long filmId) throws ValidationException, NotFoundException {
        log.debug("Поиск фильма по id на уровне сервиса");

        if (filmId == null) {
            throw new ValidationException("Передан пустой filmId");
        }
        log.debug("Передан id фильма: {}", filmId);

        Film searchResult = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        log.debug("Фильм с id {} найден в хранилище", searchResult.getId());

        FilmDto result = FilmMapper.mapToFilmDto(searchResult);

        // Заполняем коллекции
        completeDto(result);
        log.debug("Найденный фильм с id {} преобразован", result.getId());

        log.debug("Возврат результата поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для сохранения на уровень хранилища, после чего сохранённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохраненный экземпляр {@link NewFilmRequest}
     * @return сохраненный экземпляр {@link FilmDto}
     * @throws ValidationException в случае ошибок валидации
     */
    public FilmDto create(NewFilmRequest request) throws ValidationException {
        log.debug("Создание фильма на уровне сервиса");

        Film film = FilmMapper.mapToFilm(request);
        log.debug("Переданная модель преобразована");

        log.debug("Валидация переданной модели");
        validate(film);
        log.debug("Валидация модели завершена");

        film = filmStorage.createFilm(film);

        FilmDto result = FilmMapper.mapToFilmDto(film);

        completeDto(result);
        log.debug("Сохранённая модель преобразована");

        log.debug("Возврат результата добавления на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для обновления на уровень хранилища, после чего сохранённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохраненный экземпляр класса {@link UpdateFilmRequest}
     * @return сохраненный экземпляр класса {@link FilmDto}
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если фильм для обновления не найден
     */
    public FilmDto update(UpdateFilmRequest request) throws ValidationException, NotFoundException {
        log.debug("Обновление фильма на уровне сервиса");

        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        Film existingFilm = filmStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + request.getId() + " не найден"));
        log.debug("Фильм с id {} найден в хранилище для обновления", existingFilm.getId());

        Film updatedFilm = FilmMapper.updateFilmFields(existingFilm, request);

        log.debug("Валидация обновлённой модели");
        validate(updatedFilm);
        log.debug("Валидация обновлённой модели завершена");

        // Сохраняем изменения
        filmStorage.updateFilm(updatedFilm);

        FilmDto result = FilmMapper.mapToFilmDto(updatedFilm);

        // Заполняем коллекции
        completeDto(result);
        log.debug("Обновленная модель преобразована");

        log.debug("Возврат результата обновления на уровень контроллера");
        return result;
    }

    /**
     * Метод добавляет лайк пользователя в коллекцию лайков фильма
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если фильм или пользователь не найдены
     */
    public void addLike(Long filmId, Long userId) throws ValidationException, NotFoundException {
        log.debug("Добавление лайка фильму на уровне сервиса");

        if (filmId == null) {
            throw new ValidationException("Передан пустой id фильма");
        }

        if (userId == null) {
            throw new ValidationException("Передан пустой id пользователя");
        }

        // Получаем фильм из хранилища
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден в хранилище"));

        // Получаем пользователя из хранилища
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Добавляем пользователя в коллекцию пользователей, которым фильм понравился
        log.debug("Добавляем пользователя с id {} в коллекцию любителей фильма с id {}", user.getId(), film.getId());
        filmStorage.addLike(film.getId(), user.getId());

        log.debug("Возврат результата добавления лайка на уровень контроллера");
    }

    /**
     * Метод удаляет лайк пользователя из коллекции лайков фильма
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если не найдены фильм или пользователь
     */
    public void removeLike(Long filmId, Long userId) throws ValidationException, NotFoundException {
        log.debug("Удаление лайка, поставленного фильму");

        // Получаем фильм из хранилища
        if (filmId == null) {
            log.warn("Передан пустой идентификатор фильма при удалении лайка");
            throw new ValidationException("Передан пустой идентификатор фильма при удалении лайка");
        }

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден в хранилище"));

        // Получаем пользователя из хранилища
        if (userId == null) {
            log.warn("Передан пустой идентификатор пользователя при удалении лайка");
            throw new ValidationException("Передан пустой идентификатор пользователя при удалении лайка");
        }

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Удаляем лайк пользователя
        log.debug("Удаляем фильм с id {} из коллекции пользователя с id {}", film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        log.debug("Возврат результата удаления лайка на уровень контроллера");
    }

    /**
     * Метод удаляет фильм и все его связи из хранилища
     *
     * @param filmId идентификатор фильма
     * @throws NotFoundException если фильм не найден
     */
    public void deleteFilm(Long filmId) throws NotFoundException {
        log.debug("Удаление фильма на уровне сервиса");

        if (filmId == null) {
            log.warn("Передан пустой id");
            throw new ValidationException("Передан пустой id");
        }

        // Получаем фильм из хранилища
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден в хранилище"));

        // Удаляем лайки
        film.getLikes().clear();
        log.debug("У фильма с id {} удалены все лайки", film.getId());

        // Удаляем фильм
        filmStorage.deleteFilm(film.getId());

        log.debug("Возврат результатов удаления на уровень контроллера");
    }

    /**
     * Метод очищает хранилище фильмов
     */
    public void clearFilms() {
        log.debug("Очистка списка фильмов на уровне сервиса");

        // Очищаем хранилище
        filmStorage.clearFilms();
        log.debug("Все фильмы удалены");

        log.debug("Возврат результатов очистки на уровень контроллера");
    }

    /**
     * Валидация сущности {@link Film} на правильное заполнение ключевых полей
     *
     * @param film экземпляр сущности {@link Film}
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException в случае ненайденных идентификаторов из коллекций
     */
    private void validate(Film film) throws ValidationException, NotFoundException {
        // Валидация наименования
        validateName(film.getName());

        // Валидация описания
        validateDescription(film.getDescription());

        // Валидация даты релиза
        validateReleaseDate(film.getReleaseDate());

        // Валидация длительности
        validateDuration(film.getDuration());

        // Валидация рейтинга
        validateRating(film.getMpa());

        // Валидация жанров
        validateGenres(film.getGenres());
    }

    /**
     * Валидация наименования сущности {@link Film}
     *
     * @param name наименование фильма
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateName(String name) throws ValidationException {
        log.debug("Запускаем валидацию наименования");
        // Наименование не должно быть пустым
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        log.debug("Передано корректное значение name: {}", name);
        log.debug("Валидация наименования успешно завершена");
    }

    /**
     * Валидация описания сущности {@link Film}
     *
     * @param description описание сущности
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateDescription(String description) throws ValidationException {
        log.debug("Запускаем валидацию описания");
        // Описание не должно быть пустым
        if (description == null || description.isBlank()) {
            throw new ValidationException("Описание должно быть указано");
        }

        // Описание не должно быть длиннее 200 символов
        if (description.length() > 200) {
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        log.debug("Передано корректное значение description: {}", description);
        log.debug("Валидация описания успешно завершена");
    }

    /**
     * Валидация даты релиза сущности {@link Film}
     *
     * @param releaseDate дата релиза сущности
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateReleaseDate(LocalDate releaseDate) throws ValidationException {
        log.debug("Запускаем валидацию даты релиза");
        // Дата релиза не должна быть раньше 28.12.2025
        if (releaseDate != null && releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }
        log.debug("Передано корректное значение releaseDate: {}",
                releaseDate == null ? "null" : releaseDate.format(DATE_FORMATTER));
        log.debug("Валидация даты релиза успешно завершена");
    }

    /**
     * Валидация продолжительности сущности {@link Film}
     *
     * @param duration длительность сущности
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateDuration(Integer duration) throws ValidationException {
        log.debug("Запускаем валидацию длительности");
        // Длительность должна быть положительным числом
        if (duration != null && duration <= 0) {
            log.debug("Передано неположительное значение продолжительности 0 > {}", duration);
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Передано корректное значение duration: {}", duration == null ? "null" : duration);
        log.debug("Валидация длительности успешно завершена");
    }

    /**
     * Валидация рейтинга сущности {@link Film}
     *
     * @param rating экземпляр класса {@link Mpa}
     * @throws NotFoundException если рейтинг не найден
     */
    private void validateRating(Mpa rating) throws NotFoundException {
        log.debug("Запускаем валидацию рейтинга");

        // Если рейтинг указан
        if (rating != null && rating.getId() != null) {
            // Проверяем его наличие по id в хранилище
            Optional<Mpa> check = mpaStorage.findById(rating.getId());
            if (check.isEmpty()) {
                throw new NotFoundException("Рейтинг с id " + rating.getId() + " не найден");
            }
        }
        log.debug("Валидация рейтинга успешно завершена");
    }

    /**
     * Валидация коллекции жанров сущности {@link Film}
     *
     * @param genres коллекция экземпляров класса {@link Genre}
     * @throws NotFoundException если жанр не найден по идентификатору
     */
    private void validateGenres(Collection<Long> genres) throws NotFoundException {
        log.debug("Запускаем валидацию жанров");

        // Если жанры указаны
        if (!genres.isEmpty()) {
            for (Long genreId : genres) {
                Optional<Genre> check = genreStorage.findById(genreId);
                if (check.isEmpty()) {
                    throw new NotFoundException("Жанр с id " + genreId + " не найден");
                }
            }
        }
        log.debug("Валидация жанров успешно завершена");
    }

    /**
     * Метод заполняет данными коллекции DTO
     *
     * @param dto экземпляр класса {@link FilmDto}
     */
    private void completeDto(FilmDto dto) {
        if (dto != null) {
            log.debug("Формирование полей для фильма с id {}", dto.getId());

            // Заполняем коллекцию жанров фильма
            completeGenres(dto);

            // Заполняем коллекцию лайков фильма
            completeLikes(dto);
        }
    }

    /**
     * Метод заполняет данными коллекцию жанров DTO
     *
     * @param dto экземпляр класса {@link FilmDto}
     */
    private void completeGenres(FilmDto dto) {
        log.debug("Заполнение коллекций жанров фильма");

        // Получаем список жанров фильма
        Set<GenreDto> genres = genreStorage.findByFilmId(dto.getId()).stream()
                .map(GenreMapper::mapToGenreDto)
                .sorted(Comparator.comparing(GenreDto::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        log.debug("Для фильма с id {} получена коллекция жанров размером {}", dto.getId(), genres.size());

        // Устанавливаем полученную коллекцию фильму
        dto.setGenres(genres);
        log.debug("Полученная коллекция жанров установлена фильму");
    }

    /**
     * Метод заполняет данными коллекцию лайков DTO
     *
     * @param dto экземпляр класса {@link FilmDto}
     */
    private void completeLikes(FilmDto dto) {
        log.debug("Заполнение коллекций лайков фильма");

        // Получаем список лайков фильма
        Set<UserShortDto> likes = userStorage.findByFilmId(dto.getId()).stream()
                .map(UserMapper::mapToUserShortDto)
                .collect(Collectors.toSet());
        log.debug("Для фильма с id {}  получена коллекция лайков размером {}", dto.getId(), likes.size());

        // Устанавливаем полученную коллекцию фильму
        dto.setLikes(likes);
        log.debug("Полученная коллекция лайков установлена фильму");
    }
}
