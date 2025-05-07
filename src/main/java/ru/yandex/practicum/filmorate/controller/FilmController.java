package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

/***
 * Контроллер для обработки методов GET, POST и PUT для "/films"
 */
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final HashMap<Long, Film> films = new HashMap<>();

    /***
     * Обработка метода GET
     * @return коллекция сохранённых {@link Film}
     */
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрошен список пользователей");
        Collection<Film> result = films.values();
        log.debug(result.toString());
        return result;
    }

    /***
     * Обработка метода POST
     * @param film сущность {@link Film} из тела запроса
     * @return {@link Film} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Запрошено добавление фильма");
        log.debug(film.toString());

        // Проводим валидацию
        validate(film);

        // Устанавливаем id
        film.setId(getNextId());
        log.info("Фильм прошёл валидацию и получил id");
        log.debug(film.toString());

        // Сохраняем в хранилище
        films.put(film.getId(), film);
        log.info("Фильм добавлен в хранилище");

        // Возвращаем результат
        return film;
    }

    /***
     * Обработка метода PUT
     * @param newFilm сущность {@link Film} из тела запроса
     * @return {@link Film} с заполненными созданными и генерируемыми значениями
     */
    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Запрошено изменение фильма");
        log.debug(newFilm.toString());

        // Проверяем наличие id
        if (newFilm.getId() == null) {
            log.warn("При обновлении передан пустой id");
            throw new ValidationException("Id должен быть указан");
        }

        // Ищем Film в хранилище по id
        Optional<Film> existingFilmOpt = films.values().stream()
                .filter(existingFilm -> existingFilm.getId().equals(newFilm.getId())).findFirst();

        // Проверяем успешность поиска по id
        if (existingFilmOpt.isEmpty()) {
            log.warn("Фильм с id {} не найден в хранилище", newFilm.getId());
            throw new ValidationException("Фильм с id " + newFilm.getId() + " не найден");
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
        }

        // Проверим изменение даты релиза
        if (!newFilm.getReleaseDate().equals(existingFilm.getReleaseDate())) {
            log.debug("Будет изменена дата релиза фильма с {} на {}",
                    existingFilm.getReleaseDate().format(DATE_FORMATTER),
                    newFilm.getReleaseDate().format(DATE_FORMATTER));
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        // Проверим изменение длительности
        if (!newFilm.getDuration().equals(existingFilm.getDuration())) {
            log.debug("Будет изменена длительность фильма с {} на {}", existingFilm.getDuration(),
                    newFilm.getDuration());
            existingFilm.setDuration(newFilm.getDuration());
        }

        // Если изменения данных были
        if (valuesAreChanged) {
            // Проводим валидацию
            log.debug("Начинаем валидацию данных");
            validate(existingFilm);
            log.debug("Валидация данных прошла успешно");

            // СОхраняем изменения
            log.debug("Сохраняем изменения");
            films.put(existingFilm.getId(), existingFilm);
            log.info("Изменения сохранены");
        } else {
            log.info("Изменения не обнаружены");
        }

        // Возвращаем результат
        return newFilm;
    }

    /***
     * Генерация id для {@link Film}
     * @return Следующее значение для id
     */
    private Long getNextId() {
        return films.values().stream().map(Film::getId).max(Long::compareTo).orElse(0L) + 1;
    }

    /***
     * Валидация сущности {@link Film} на заполнение ключевых полей.
     * Вызывает {@link ValidationException} при нарушении правил
     * @param film экземпляр сущности {@link Film}
     */
    private void validate(Film film) throws ValidationException {
        // Валидация названия
        log.debug("Запускаем валидацию названия");
        validateName(film.getName());
        log.debug("Валидация названия успешно завершена");

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

    /***
     * Валидация названия сущности {@link Film}.
     * Вызывает исключение {@link ValidationException}
     * @param name название фильма
     */
    private void validateName(String name) throws ValidationException {
        if (name == null || name.isBlank()) {
            log.warn("Передано пустое название");
            throw new ValidationException("Название не может быть пустым");
        }
    }

    /***
     * Валидация названия сущности {@link Film}.
     * Вызывает исключение {@link ValidationException}
     * @param description описание фильма
     */
    private void validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            log.debug("Передано пустое описание");
            throw new ValidationException("Описание не может быть пустым");
        }

        if (description.length() > 200) {
            log.debug("Передано описание длинной больше 200 символов ({}, {})", description, description.length());
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
    }

    /***
     * Валидация даты релиза сущности {@link Film}.
     * Вызывает исключение {@link ValidationException}
     * @param releaseDate дата релиза
     */
    private void validateReleaseDate(LocalDate releaseDate) {
        LocalDate cinemaStart = LocalDate.of(1895, 12, 28);
        if (releaseDate.isBefore(cinemaStart)) {
            log.debug("Дата релиза {} раньше, чем {}", releaseDate.format(DATE_FORMATTER),
                    cinemaStart.format(DATE_FORMATTER));
            throw new ValidationException("Дата релиза не может быть раньше " + cinemaStart.format(DATE_FORMATTER));
        }
    }

    /***
     * Валидация длительности сущности {@link Film}.
     * Вызывает исключение {@link ValidationException}
     * @param duration продолжительность
     */
    private void validateDuration(Integer duration) {
        if (duration <= 0) {
            log.debug("Продолжительность должна быть положительным числом ({})", duration);
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
    }
}
