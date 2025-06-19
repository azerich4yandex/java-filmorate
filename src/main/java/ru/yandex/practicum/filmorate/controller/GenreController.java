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
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.db.dto.create.NewGenreRequest;
import ru.yandex.practicum.filmorate.storage.db.dto.read.GenreDto;
import ru.yandex.practicum.filmorate.storage.db.dto.update.UpdateGenreRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    /**
     * Обработка GET-запроса на /genres
     *
     * @param size максимальный размер коллекции
     * @param from номер начального элемента
     * @return коллекция {@link GenreDto}
     */
    @GetMapping
    public ResponseEntity<Collection<GenreDto>> findAll(@RequestParam(name = "size", defaultValue = "10") Integer size,
                                                        @RequestParam(name = "from", defaultValue = "0") Integer from) {
        log.info("Запрос всех жанров на уровне контроллера");
        log.debug("Размер коллекции: {}", size);
        log.debug("Стартовый номер элемента: {}", from);

        Collection<GenreDto> result = genreService.findAll(size, from);
        log.info("На уровень контроллера вернулась коллекция размером {}", result.size());

        log.info("Возврат результатов на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса к /genres/{id}
     *
     * @param genreId идентификатор жанра
     * @return найденный экземпляр класса {@link GenreDto}
     */
    @GetMapping("/{id}")
    public GenreDto findById(@PathVariable(name = "id") Long genreId) {
        log.info("Поиск жанра по id на уровне контроллера");
        log.debug("Передан id: {}", genreId);

        GenreDto result = genreService.findById(genreId);
        log.debug("На уровень контроллера вернулся жанр с id {}", result.getId());

        log.info("Возврат результата на уровень клиента");
        return result;
    }

    /**
     * Обработка POST-запроса для /genres
     *
     * @param request сущность {@link NewGenreRequest} из тела запроса
     * @return {@link GenreDto} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<GenreDto> create(@RequestBody NewGenreRequest request) {
        log.info("Запрошено создание жанра на уровне контроллера");

        GenreDto result = genreService.create(request);
        log.debug("На уровень контроллера после добавления вернулся жанр с id {}", result.getId());

        log.info("Возврат результата добавления на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса для /genres
     *
     * @param request сущность {@link UpdateGenreRequest} из тела запроса
     * @return жанр с измененными значениями
     */
    @PutMapping
    public ResponseEntity<GenreDto> update(@RequestBody UpdateGenreRequest request) {
        log.info("Запрошено изменение жанра на уровне контроллера");

        GenreDto result = genreService.update(request);
        log.debug("На уровень контроллера после изменения вернулся жанр с id {}", result.getId());

        log.info("Возврат результатов обновления на уровень контроллера");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса к /genres/{id}
     *
     * @param genreId идентификатор жанра
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable(name = "id") Long genreId) {
        log.info("Запрошено удаление жанра на уровне контроллера");
        log.debug("Передан id удаляемого жанра: {}", genreId);

        genreService.deleteGenre(genreId);

        log.info("Возврат результатов удаления жанра на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса к /genres
     */
    @DeleteMapping
    public ResponseEntity<Void> clearGenres() {
        log.info("Запрошена очистка списка жанров на уровне контроллера");

        genreService.clearGenres();

        log.info("Возврат результатов очистки списка жанров на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
