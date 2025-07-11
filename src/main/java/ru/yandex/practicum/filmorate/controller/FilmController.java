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
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

/**
 * Контроллер для обработки HTTP-запросов для /films
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    /**
     * Обработка GET-запроса на /films
     *
     * @param size максимальный размер коллекции
     * @param from номер начального элемента
     * @return коллекция {@link FilmDto}
     */
    @GetMapping
    public ResponseEntity<Collection<FilmDto>> findAll(@RequestParam(name = "size", defaultValue = "10") Integer size,
                                                       @RequestParam(name = "from", defaultValue = "0") Integer from) {
        log.info("Запрос всех пользователей на уровне контроллера");
        log.debug("Размер коллекции: {}", size);
        log.debug("Стартовый номер элемента: {}", from);

        Collection<FilmDto> result = filmService.findAll(size, from);
        log.debug("На уровень контроллера вернулась коллекция размером {}", result.size());

        log.info("Возврат результатов на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /films/{id}
     *
     * @param id идентификатор фильма
     * @return экземпляр класса {@link FilmDto}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> findById(@PathVariable Long id) {
        log.info("Поиск фильма по id на уровне контроллера");
        log.debug("Передан id: {}", id);

        FilmDto film = filmService.findById(id);
        log.debug("На уровень контроллера успешно вернулся фильм с id {}", film.getId());

        log.info("Возврат результата на уровень клиента");
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /films/popular?count={limit}&genreId={genreId}&year={year}
     */
    @GetMapping("/popular?count={limit}&genreId={genreId}&year={year}")
    public ResponseEntity<Collection<FilmDto>> findPopular(
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer count,
            @RequestParam(name = "genreId", required = false) Long genreId,
            @RequestParam(name = "year", required = false) Integer year) {
        log.info("Поиск топ фильмов на уровне контроллера");
        log.debug("Передано значение count = {}", count);
        log.debug("Передано значение genreId = {}", genreId);
        log.debug("Передано значение year = {}", year);

        Collection<FilmDto> result = filmService.findPopular(count, genreId, year);
        log.debug("На уровень контроллера вернулась коллекция топ-фильмов размером {}", result.size());

        log.info("Возвращение топ фильмов на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка POST-запроса для /films
     *
     * @param request сущность {@link NewFilmRequest} из тела запроса
     * @return {@link FilmDto} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<FilmDto> create(@RequestBody NewFilmRequest request) {
        log.info("Запрошено добавление фильма на уровне контроллера");

        FilmDto result = filmService.create(request);
        log.debug("На уровень контроллера после добавления успешно вернулся фильм с id {}", result.getId());

        log.info("Возврат результата создания на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса для /films
     *
     * @param request несохранённый фильм с изменениями
     * @return фильм с сохранёнными изменениями
     */
    @PutMapping
    public ResponseEntity<FilmDto> update(@RequestBody UpdateFilmRequest request) {
        log.info("Запрошено изменение фильма на уровне контроллера");
        log.debug("Передан для обновления фильм с id {}", request.getId());

        FilmDto updatedFilm = filmService.update(request);
        log.debug("На уровень контроллера после изменения вернулся фильм с id {}", updatedFilm.getId());

        log.info("Возврат результата обновления на уровень клиента");
        return new ResponseEntity<>(updatedFilm, HttpStatus.OK);
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

        log.info("Возврат результата добавления на уровень клиента");
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

        log.info("Возврат результата удаления лайка на уровень клиента");
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

        log.info("Возврат результата удаления на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /films
     */
    @DeleteMapping
    public ResponseEntity<Void> clearFilms() {
        log.info("Запрошена очистка списка пользователей на уровне контроллера");

        filmService.clearFilms();

        log.info("Возврат результатов очистки на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}