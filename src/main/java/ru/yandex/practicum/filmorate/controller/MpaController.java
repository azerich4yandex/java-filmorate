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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.db.dto.create.NewMpaRequest;
import ru.yandex.practicum.filmorate.storage.db.dto.read.MpaDto;
import ru.yandex.practicum.filmorate.storage.db.dto.update.UpdateMpaRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    private final MpaService mpaService;

    /**
     * Обработка GET-запроса к /mpa
     *
     * @param size максимальный размер коллекции
     * @param from номер начального элемента
     * @return результирующая коллекция
     */
    @GetMapping
    public ResponseEntity<Collection<MpaDto>> findAll(@RequestParam(name = "size", defaultValue = "10") Integer size,
                                                      @RequestParam(name = "from", defaultValue = "0") Integer from) {
        log.info("Запрос всех рейтингов на уровне контроллера");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер начального элемента: {}", from);

        Collection<MpaDto> result = mpaService.findAll(size, from);
        log.debug("На уровень контроллера вернулась коллекция размером {}", result.size());

        log.info("Возврат результатов на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса к /mpa/{id}
     *
     * @param ratingId идентификатора рейтинга
     * @return найденный экземпляр класса {@link Mpa}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MpaDto> findById(@PathVariable(name = "id") Long ratingId) {
        log.info("Поиск рейтинга по id на уровне контроллера");
        log.debug("Передан id: {}", ratingId);

        MpaDto result = mpaService.findById(ratingId);
        log.debug("На уровень контроллера вернулся рейтинг с id {}", result.getId());

        log.info("Возврат результата на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка POST-запроса к /mpa
     *
     * @param request сущность {@link NewMpaRequest} из тела запроса
     * @return {@link MpaDto} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<MpaDto> create(@RequestBody NewMpaRequest request) {
        log.info("Запрошено создание рейтинга на уровне контроллера");

        MpaDto result = mpaService.create(request);
        log.debug("На уровень контроллера вернулся рейтинг после добавления с id {}", result.getId());

        log.info("Возврат результатов добавления на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса к /mpa
     *
     * @param request сущность {@link UpdateMpaRequest} из тела запроса
     * @return {@link Mpa} с измененными значениями
     */
    @PutMapping
    public ResponseEntity<MpaDto> update(@RequestBody UpdateMpaRequest request) {
        log.info("Запрошено обновление рейтинга на уровне контроллера");

        MpaDto rating = mpaService.update(request);
        log.debug("На уровень контроллера после обновления вернулся рейтинг с id {}", rating.getId());

        log.info("Возврат результатов обновления на уровень клиента");
        return new ResponseEntity<>(rating, HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса к /mpa/{id}
     *
     * @param ratingId идентификатор рейтинга
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable(name = "id") Long ratingId) {
        log.info("Запрошено удаление рейтинга на уровне контроллера");

        mpaService.deleteRating(ratingId);

        log.info("Возврат результатов удаления на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса к /mpa
     */
    @DeleteMapping
    public ResponseEntity<Void> clearRatings() {
        log.info("Запрошена очистка списка рейтингов на уровне контроллера");

        mpaService.clearGenres();

        log.info("Возврат результатов очистки списка рейтингов на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
