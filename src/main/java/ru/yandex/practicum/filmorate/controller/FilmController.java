package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

/**
 * Контроллер для обработки HTTP-запросов для /films
 */
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    /**
     * Обработка GET-запроса на /films
     *
     * @return коллекция сохранённых {@link Film}
     */
    @GetMapping
    public ResponseEntity<Collection<Film>> findAll() {
        log.info("Запрос всех пользователей на уровне контроллера");

        Collection<Film> result = filmService.findAll();
        log.debug("На уровень контроллера вернулась коллекция размером {}", result.size());

        log.info("Возврат результатов на уровень пользователя");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /films/{id}
     *
     * @param id идентификатор {@link Film}
     * @return экземпляр класса {@link Film} или null
     */
    @GetMapping("/{id}")
    public ResponseEntity<Film> findById(@PathVariable Long id) {
        log.info("Поиск фильма по id на уровне контроллера");
        log.debug("Передан id: {}", id);

        Film film = filmService.findById(id);
        log.debug("На уровень контроллера успешно вернулся фильм с id {}", film.getId());

        log.info("Возврат результата на уровень пользователя");
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /films/popular?count={count}
     */
    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> findPopular(
            @RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("Поиск топ фильмов на уровне контроллера");
        log.debug("Передано значение count = {}", count);

        Collection<Film> result = filmService.findPopular(count);
        log.debug("На уровень контроллера вернулась коллекция топ-фильмов размером {}", result.size());

        log.info("Возвращение топ фильмов на уровень пользователя");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка POST-запроса для /films
     *
     * @param film сущность {@link Film} из тела запроса
     * @return {@link Film} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<Film> create(@RequestBody Film film) {
        log.info("Запрошено добавление фильма на уровне контроллера");

        film = filmService.create(film);
        log.debug("На уровень контроллера после добавления успешно вернулся фильм с id {}", film.getId());

        log.info("Возврат результата создания на уровень пользователя");
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса для /films
     *
     * @param film сущность {@link Film} из тела запроса
     * @return {@link Film} с заполненными созданными и генерируемыми значениями
     */
    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film film) {
        log.info("Запрошено изменение фильма на уровне контроллера");
        log.debug("Передан для обновления фильм с id {}", film.getId());

        Film existingFilm = filmService.update(film);
        log.debug("На уровень контроллера после изменения вернулся фильм с id {}", existingFilm.getId());

        log.info("Возврат результата обновления на уровень пользователя");
        return new ResponseEntity<>(existingFilm, HttpStatus.OK);
    }

    /**
     * Обработка PUT-запроса для /films/{filmId}/like/{userId}
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId) {
        log.info("Запрошено добавление лайка на уровне контроллера");
        log.debug("Передан id фильма: {}", filmId);
        log.debug("Передан id пользователя: {}", userId);

        filmService.addLike(filmId, userId);

        log.info("Возврат результата добавления на уровень пользователя");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /films/{id}/like/{userId}
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId) {
        log.info("Запрошено удаление лайка на уровне контроллера");
        log.debug("Передан id  фильма: {}", filmId);
        log.debug("Передан id  пользователя: {}", userId);

        filmService.removeLike(filmId, userId);

        log.info("Возврат результата удаления лайка на уровень пользователя");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /films/{id}
     *
     * @param filmId идентификатор фильма
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable(name = "id") Long filmId) {
        log.info("Запрошено удаление фильма на уровне контроллера");
        log.debug("Передан id удаляемого фильма: {}", filmId);

        filmService.deleteFilm(filmId);

        log.info("Возврат результата удаления на уровень пользователя");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /films
     */
    @DeleteMapping
    public ResponseEntity<Void> clearFilms() {
        log.info("Запрошена очистка списка пользователей на уровне контроллера");

        filmService.clearFilms();

        log.info("Возврат результатов очистки на уровень пользователя");
        return new ResponseEntity<>(HttpStatus.OK);
    }


}