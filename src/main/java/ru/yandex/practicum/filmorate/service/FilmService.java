package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
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

        Collection<Film> films = filmStorage.findAll();

        log.debug("Возврат результатов поиска на уровень контроллера");
        return films;
    }

    /**
     * Метод возвращает экземпляр класса {@link Film}, найденный по идентификатору
     *
     * @param filmId идентификатор фильма
     * @return экземпляр класса {@link Film}
     * @throws ValidationException если передан пустой filmId
     */
    public Film findById(Long filmId) throws ValidationException {
        log.debug("Поиск фильма по filmId на уровне сервиса");

        if (filmId == null) {
            throw new ValidationException("Передан пустой filmId");
        }

        Optional<Film> filmOpt = filmStorage.findById(filmId);

        if (filmOpt.isEmpty()) {
            log.warn("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        log.debug("Возврат результата поиска на уровень контроллера");
        return filmOpt.get();
    }

    /**
     * Метод получает список из {@link Film} на основе размера коллекции {@link Film#getLikes()}
     *
     * @param count размер возвращаемой коллекции
     * @return результирующая коллекция
     */
    public Collection<Film> findPopular(Integer count) {
        log.debug("Поиск топ фильмов на уровне сервиса");

        if (count == null) {
            count = 10;
        }

        if (count <= 0) {
            log.warn("Передано некорректное значение count: {}", count);
            throw new ValidationException("Значение count должно быть больше нуля");
        }

        Collection<Film> result = findAll().stream().filter(Objects::nonNull).filter(film -> !film.getLikes().isEmpty())
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed()).limit(count).toList();

        log.debug("Возврат топ-{} поиска на уровень контроллера", count);
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

        log.debug("Валидация полученной модели");
        validate(newFilm);
        log.debug("Валидация модели  завершена");

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        Optional<Film> existingFilmOpt = filmStorage.findById(newFilm.getId());

        if (existingFilmOpt.isEmpty()) {
            log.warn("Фильм с id {} не найден для обновления", newFilm.getId());
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }

        Film existingFilm = existingFilmOpt.get();
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
        } else {
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

        // Получаем фильм из хранилища
        Optional<Film> filmOpt = filmStorage.findById(filmId);
        Film film;
        if (filmOpt.isPresent()) {
            film = filmOpt.get();
        } else {
            log.warn("Фильм с id {} не найден в  хранилище", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден в хранилище");
        }

        // Получаем пользователя из хранилища
        Optional<User> userOpt = userStorage.findById(userId);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            log.warn("Пользователь с id {} не найден в хранилище", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден в хранилище");
        }

        // Добавляем пользователя в коллекцию пользователей, которым фильм понравился
        log.debug("Добавляем пользователя с id {} в коллекцию любителей фильма с id {}", user.getId(), film.getId());
        film.addUsersLike(user);

        // Сохраняем изменения фильма в хранилище
        filmStorage.save(film);

        // Добавляем фильм в коллекцию понравившихся пользователю фильмов
        log.debug("Добавляем фильм с id {} в коллекцию фильмов пользователя с id {}", film.getId(), user.getId());
        user.addLike(film);

        // Сохраняем изменение пользователя в хранилище
        userStorage.save(user);

        log.debug("Отличный фильм!");
    }

    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка, поставленного фильму");

        // Получаем фильм из хранилища
        if (filmId == null) {
            log.warn("Передан пустой идентификатор фильма при удалении лайка");
            throw new ValidationException("Передан пустой идентификатор фильма при удалении лайка");
        }

        Optional<Film> filmOpt = filmStorage.findById(filmId);
        Film film;

        if (filmOpt.isPresent()) {
            film = filmOpt.get();
        } else {
            log.warn("Фильм с id {} не найден в хранилище ", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден в хранилище");
        }

        // Получаем пользователя из хранилища
        if (userId == null) {
            log.warn("Передан пустой идентификатор пользователя при удалении лайка");
            throw new ValidationException("Передан пустой идентификатор пользователя при удалении лайка");
        }
        Optional<User> userOpt = userStorage.findById(userId);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            log.warn("Пользователь с id {} не найдена хранилище ", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден в хранилище");
        }

        // Удаляем лайк пользователя
        log.debug("Удаляем фильм с id {} из коллекции пользователя с id {}", film.getId(), user.getId());
        film.removeUsersLike(userId);

        // Сохраняем изменение фильма в хранилище
        filmStorage.save(film);

        // Удаляем пользователя из почитателей
        log.debug("Удаляем пользователя с id {} из числа почитателей фильма с id {}", user.getId(), film.getId());
        user.removeLike(film.getId());

        // Сохраняем изменение пользователя
        userStorage.save(user);

        log.debug("Фильм уже не торт!");
    }

    public void deleteFilm(Long filmId) {
        log.debug("Удаление фильма на уровне сервиса");

        if (filmId == null) {
            log.warn("Передан пустой id");
            throw new ValidationException("Передан пустой id");
        }

        // Получаем фильм из хранилища
        Optional<Film> filmOpt = filmStorage.findById(filmId);
        Film film;
        if (filmOpt.isPresent()) {
            film = filmOpt.get();
        } else {
            log.warn("Фильм с id {} не найден в хранилище", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден в хранилище");
        }

        // Проверяем наличие лайков
        if (!film.getLikes().isEmpty()) {
            // Если лайки есть
            log.debug("У фильма есть почитатели");
            for (User user : film.getLikes().values()) {
                // Удаляем каждый лайк фильма
                user.removeLike(film.getId());
                log.debug("Фильм больше не нравится пользователю c id {}", user.getId());

                // И сохраняем изменение пользователя
                userStorage.save(user);
            }
        }

        // Удаляем фильм
        filmStorage.delete(film.getId());

        log.debug("Возврат результатов удаления на уровень контроллера");
    }

    public void clearFilms() {
        log.debug("Очистка списка фильмов на уровне сервиса");

        // Получаем всех пользователей с избранными фильмами
        for (User user : userStorage.findAll().stream().filter(u -> !u.getLikes().isEmpty()).toList()) {
            // Очищаем лайки
            user.getLikes().clear();
            log.debug("Пользователю с id {} больше не нравятся фильмы", user.getId());

            // Сохраняем изменения
            userStorage.save(user);
        }

        // Очищаем хранилище
        filmStorage.clear();

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
        log.debug("Запускаем валидацию наименования");
        validateName(film.getName());
        log.debug("Валидация наименования успешно завершена");

        // Валидация описания
        log.debug("Запускаем валидацию описания");
        validateDescription(film.getDescription());
        log.debug("Валидация описания успешно завершена");

        // Валидация даты релиза
        log.debug("Запускаем валидацию даты релиза");
        validateReleaseDate(film.getReleaseDate());
        log.debug("Валидация даты релиза успешно завершена");

        // Валидация длительности
        log.debug("Запускаем валидацию длительности");
        validateDuration(film.getDuration());
        log.debug("Валидация длительности успешно завершена");
    }

    /**
     * Валидация наименования сущности {@link Film}
     *
     * @param name наименование фильма
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateName(String name) throws ValidationException {
        // Наименование не должно быть пустым
        if (name == null || name.isBlank()) {
            log.debug("Передано пустое наименование");
            throw new ValidationException("Название не может быть пустым");
        }
    }

    /**
     * Валидация описания сущности {@link Film}
     *
     * @param description описание сущности
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateDescription(String description) throws ValidationException {
        // Описание не должно быть пустым
        if (description == null || description.isBlank()) {
            log.debug("Передано пустое описание");
            throw new ValidationException("Описание должно быть указано");
        }

        // Описание не должно быть длиннее 200 символов
        if (description.length() > 200) {
            log.debug("Передано описание длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
    }

    /**
     * Валидация даты релиза сущности {@link Film}
     *
     * @param releaseDate дата релиза сущности
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateReleaseDate(LocalDate releaseDate) throws ValidationException {
        // Дата релиза не должна быть раньше 28.12.2025
        if (releaseDate != null && releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.debug("Передана дата релиза раньше 28.12.2025 > {}", releaseDate.format(DATE_FORMATTER));
            throw new ValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }
    }

    /**
     * Валидация продолжительности сущности {@link Film}
     *
     * @param duration длительность сущности
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateDuration(Integer duration) throws ValidationException {
        // Длительность должна быть положительным числом
        if (duration != null && duration <= 0) {
            log.debug("Передано неположительное значение продолжительности 0 > {}", duration);
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
