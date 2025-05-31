package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

/**
 * Класс предварительной обработки и валидации сущностей {@link User} на уровне сервиса
 */
@Slf4j
@Service
public class FilmService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    /**
     * Метод возвращает коллекцию {@link Film}
     *
     * @return коллекция {@link Film}
     */
    public Collection<Film> findAll() {
        log.debug("Запрос всех фильмов на уровне сервиса");

        Collection<Film> result = filmStorage.findAll();
        log.debug("На уровень сервиса вернулась коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link Film}, найденный по идентификатору
     *
     * @param filmId идентификатор фильма
     * @return экземпляр класса {@link Film}
     * @throws ValidationException если передан пустой filmId
     */
    public Film findById(Long filmId) throws ValidationException {
        log.debug("Поиск фильма по id на уровне сервиса");

        if (filmId == null) {
            throw new ValidationException("Передан пустой filmId");
        }
        log.debug("Передан id фильма: {}", filmId);

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        log.debug("Фильм с id {} найден в хранилище", film.getId());

        log.debug("Возврат результата поиска на уровень контроллера");
        return film;
    }

    /**
     * Метод получает список из {@link Film} на основе размера коллекции {@link Film#getLikes()}
     *
     * @param count размер возвращаемой коллекции
     * @return результирующая коллекция
     */
    public Collection<Film> findPopular(Integer count) {
        log.debug("Поиск топ фильмов на уровне сервиса");

        // Если аннотация в параметре контроллера была проигнорирована (запуск тестов напрямую без Mock)
        if (count == null) {
            count = 10;
        }

        // Если передано отрицательное значение
        if (count <= 0) {
            throw new ValidationException("Значение count должно быть больше нуля");
        }

        Collection<Film> result = findAll().stream().filter(film -> !film.getLikes().isEmpty())
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed()).limit(count).toList();
        log.debug("Получена коллекция топ-фильмов размером {}", result.size());

        log.debug("Возврат коллекции топ-фильмов на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель {@link Film} и передает для сохранения на уровень хранилища, после чего
     * сохранённую модель возвращает на уровень контроллера
     *
     * @param film несохраненный экземпляр {@link Film}
     * @return сохраненный экземпляр {@link Film}
     */
    public Film create(Film film) {
        log.debug("Создание фильма на уровне сервиса");

        log.debug("Валидация переданной модели");
        validate(film);
        log.debug("Валидация модели завершена");

        film = filmStorage.create(film);

        log.debug("Возврат результата добавления на уровень контроллера");
        return film;
    }

    /**
     * Метод проверяет полученную модель {@link Film} и передает для обновления на уровень хранилища, после чего
     * сохранённую модель возвращает на уровень контроллера
     *
     * @param newFilm несохраненный экземпляр класса {@link Film}
     * @return сохраненный экземпляр класса {@link Film}
     * @throws ValidationException в случае ошибок валидации
     */
    public Film update(Film newFilm) throws ValidationException {
        log.debug("Обновление фильма на уровне сервиса");

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        log.debug("Валидация полученной модели");
        validate(newFilm);
        log.debug("Валидация модели  завершена");

        Film existingFilm = filmStorage.findById(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + newFilm.getId() + " не найден"));
        log.debug("Фильм с id {} найден в хранилище для обновления", existingFilm.getId());
        boolean valuesAreChanged = false;

        // Проверим изменение названия
        if (!newFilm.getName().equals(existingFilm.getName())) {
            log.debug("Будет изменено название фильма с {} на {}", existingFilm.getName(), newFilm.getName());
            existingFilm.setName(newFilm.getName());
            valuesAreChanged = true;
        }

        // Проверим изменение описания
        if (!newFilm.getDescription().equals(existingFilm.getDescription())) {
            log.debug("Будет изменено описание фильма с {} на {}", existingFilm.getDescription(),
                    newFilm.getDescription());
            existingFilm.setDescription(newFilm.getDescription());
            valuesAreChanged = true;
        }

        // Проверим изменение даты релиза
        if (newFilm.getReleaseDate() != null) {
            if (!newFilm.getReleaseDate().equals(existingFilm.getReleaseDate())) {
                log.debug("Будет изменена дата релиза фильма с {} на {}",
                        existingFilm.getReleaseDate().format(DATE_FORMATTER),
                        newFilm.getReleaseDate().format(DATE_FORMATTER));

                existingFilm.setReleaseDate(newFilm.getReleaseDate());

                valuesAreChanged = true;
            }
        } else {
            log.debug("Будет удалена дата релиза");

            existingFilm.setReleaseDate(null);

            valuesAreChanged = true;
        }

        // Проверим изменение длительности
        if (newFilm.getDuration() != null) {
            if (!newFilm.getDuration().equals(existingFilm.getDuration())) {
                log.debug("Будет изменена длительность фильма с {} на {}", existingFilm.getDuration(),
                        newFilm.getDuration());

                existingFilm.setDuration(newFilm.getDuration());

                valuesAreChanged = true;
            }
        } else if (existingFilm.getDuration() != null) {
            log.debug("Будет удалена продолжительность");

            existingFilm.setDuration(null);

            valuesAreChanged = true;
        }

        // Если изменения данных были
        if (valuesAreChanged) {
            // Проводим валидацию
            log.debug("Валидация обновлённой модели");
            validate(existingFilm);
            log.debug("Валидация обновлённой модели завершена");

            // Сохраняем изменения
            filmStorage.update(existingFilm);
        } else {
            log.info("Изменения не обнаружены");
        }

        log.debug("Возврат результата обновления на уровень контроллера");
        return existingFilm;
    }

    /**
     * Метод добавляет фильм в коллекцию понравившихся пользователю фильмов и пользователя в коллекцию почитателей
     * фильма
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     * @throws RuntimeException при неожиданной ошибке
     */
    public void addLike(Long filmId, Long userId) throws RuntimeException {
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
        film.getLikes().add(user.getId());

        // Сохраняем изменения фильма в хранилище
        filmStorage.save(film);

        log.debug("Возврат результата добавления лайка на уровень контроллера");
    }

    public void removeLike(Long filmId, Long userId) {
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
        film.getLikes().remove(userId);

        // Сохраняем изменение фильма в хранилище
        filmStorage.save(film);

        log.debug("Возврат результата удаления лайка на уровень контроллера");
    }

    public void deleteFilm(Long filmId) {
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
        filmStorage.delete(film.getId());

        log.debug("Возврат результатов удаления на уровень контроллера");
    }

    public void clearFilms() {
        log.debug("Очистка списка фильмов на уровне сервиса");

        // Очищаем хранилище
        filmStorage.clear();
        log.debug("Все фильмы удалены");

        log.debug("Возврат результатов очистки на уровень контроллера");
    }

    /**
     * Валидация сущности {@link Film} на правильное заполнение ключевых полей
     *
     * @param film экземпляр сущности {@link Film}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validate(Film film) throws ValidationException {
        // Валидация наименования
        validateName(film.getName());

        // Валидация описания
        validateDescription(film.getDescription());

        // Валидация даты релиза
        validateReleaseDate(film.getReleaseDate());

        // Валидация длительности
        validateDuration(film.getDuration());
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
            log.debug("Передано пустое наименование");
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
}
