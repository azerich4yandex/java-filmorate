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
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.service.DirectorService;

/**
 * Контроллер для обработкиHTTP-запросов для /director
 */
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/directors")
@RestController
public class DirectorController {

    private final DirectorService directorService;

    /**
     * Обработка GET-запроса для /directors
     *
     * @param size максимальный размер коллекции
     * @param from номер начального элемента
     * @return коллекция {@link DirectorDto}
     */
    @GetMapping
    public ResponseEntity<Collection<DirectorDto>> findAll(@RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                                           @RequestParam(name = "from", required = false, defaultValue = "0") Integer from) {
        log.info("Запрос всех режиссеров на уровне контроллера");
        log.debug("Размер коллекции: {}", size);
        log.debug("Стартовый номер элемента: {}", from);

        Collection<DirectorDto> result = directorService.findAll(size, from);
        log.debug("На уровень контроллера вернулась коллекция размером {}", result.size());

        log.info("Возврат результатов на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка GET-запроса для /directors/{id}
     *
     * @param directorId идентификатор режиссера
     * @return экземпляр класса {@link DirectorDto}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DirectorDto> findById(@PathVariable(name = "id") Long directorId) {
        log.info("Запрос режиссера по идентификатору на уровне контроллера");
        log.debug("Передан id режиссера: {}", directorId);

        DirectorDto result = directorService.findById(directorId);
        log.debug("На уровень контроллера успешно вернулся режиссер с id {}", directorId);

        log.info("Возврат результата поиска на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка POST-запроса для /directors
     *
     * @param request сущность {@link NewDirectorRequest} из тела запроса
     * @return {@link DirectorDto} с заполненными созданными и генерируемыми значениями
     */
    @PostMapping
    public ResponseEntity<DirectorDto> create(@RequestBody NewDirectorRequest request) {
        log.info("Запрошено добавление режиссера на уровне контроллера");

        DirectorDto result = directorService.create(request);
        log.debug("На уровень контроллера после добавления вернулся режиссер с id {}", result.getId());

        log.info("Возврат результатов добавления на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Обработка PUT-запроса для /directors
     *
     * @param request несохраненный режиссер с изменениями
     * @return режиссер сохраненными изменениями
     */
    @PutMapping
    public ResponseEntity<DirectorDto> update(@RequestBody UpdateDirectorRequest request) {
        log.info("Запрошено изменение режиссера на уровне контроллера");
        log.debug("Передан для обновления режиссер с id: {}", request.hasId() ? request.getId() : "null");

        DirectorDto result = directorService.update(request);
        log.debug("На уровень контроллера после изменения вернулся режиссер с id {}", result.getId());

        log.info("Возврат результатов изменения на уровень клиента");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /directors/{id}
     *
     * @param directorId идентификатор режиссера
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long directorId) {
        log.info("Запрошено удаление режиссера на уровне контроллера");
        log.debug("Передан идентификатор режиссера: {}", directorId == null ? "null" : directorId);

        directorService.deleteDirector(directorId);

        log.info("Возврат результатов удаления на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Обработка DELETE-запроса для /directors
     */
    @DeleteMapping
    public ResponseEntity<Void> clearDirectors() {
        log.debug("Запрошена очистка хранилища режиссеров на уровне контроллера");

        directorService.clearDirectors();

        log.info("Возврат результатов очистки на уровень клиента");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
