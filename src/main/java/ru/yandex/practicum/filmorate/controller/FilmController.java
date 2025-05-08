package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
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
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрошено добавление фильма");
        log.debug(film.toString());

        // Устанавливаем id
        film.setId(getNextId());
        log.info("Фильм получил id");
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
    public Film update(@Valid @RequestBody Film newFilm) {
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
            // Сохраняем изменения
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
}
