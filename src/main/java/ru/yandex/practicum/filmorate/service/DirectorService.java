package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.director.DirectorStorage;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.director.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

/**
 * Класс предварительное обработки и валидации сущностей {@link Director}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    /**
     * Метод возвращает коллекцию {@link DirectorDto}
     *
     * @param size максимальный размер коллекции
     * @param from номер стартового элемента
     * @return результирующая коллекция
     */
    public Collection<DirectorDto> findAll(Integer size, Integer from) {
        log.debug("Запрос всех режиссеров на уровне сервиса");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Director> searchResult = directorStorage.findAll(size, from);
        log.debug("На уровень сервиса вернулась коллекция размером {}", searchResult.size());

        Collection<DirectorDto> result = searchResult.stream().map(DirectorMapper::mapToDirectorDto).toList();
        log.debug("Найденная коллекция преобразована. Размер коллекции после преобразования: {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    public Collection<DirectorDto> findByFilmId(Long filmId) {
        log.debug("Поиск режиссеров, связанных с фильмом на уровне сервиса");

        if (filmId == null) {
            throw new ValidationException("Передан пустой filmId");
        }

        log.debug("Передан идентификатор фильма: {}", filmId);

        Collection<Director> searchResult = directorStorage.findByFilmId(filmId);
        log.debug("На уровень сервиса вернулась коллекция режиссеров размером {}", searchResult.size());

        Collection<DirectorDto> result = searchResult.stream().map(DirectorMapper::mapToDirectorDto).toList();
        log.debug("Найденная коллекция режиссеров преобразована. Размер коллекции после преобразования: {}",
                result.size());

        log.debug("Возврат результатов поиска режиссеров на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link DirectorDto}, найденный по идентификатору
     *
     * @param directorId идентификатор режиссера
     * @return экземпляр класса {@link DirectorDto}
     */
    public DirectorDto findById(Long directorId) {
        log.debug("Поиск режиссера по id на уровне сервиса");

        if (directorId == null) {
            throw new ValidationException("Передан пустой directorId");
        }

        log.debug("Передан идентификатор режиссера: {}", directorId);

        Director searchResult = directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + directorId + " не найден"));
        log.debug("Режиссер с id {} найден в хранилище", searchResult.getId());

        DirectorDto result = DirectorMapper.mapToDirectorDto(searchResult);

        log.debug("Возврат результата поиска экземпляра на уровень хранилища");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для сохранения на уровень хранилища, после чего сохраненную модель
     * возвращает на уровень контроллера
     *
     * @param request несохраненный экземпляр {@link NewDirectorRequest}
     * @return сохраненный экземпляр {@link DirectorDto}
     */
    public DirectorDto create(NewDirectorRequest request) {
        log.debug("Запрошено создание режиссера на уровне сервиса");

        Director director = DirectorMapper.mapToDirector(request);
        log.debug("Переданная модель преобразована");

        log.debug("Валидация переданной модели");
        validate(director);
        log.debug("Валидация переданной модели завершена");

        director = directorStorage.createDirector(director);

        DirectorDto result = DirectorMapper.mapToDirectorDto(director);
        log.debug("Сохраненная модель преобразована");

        log.debug("Возврат результатов добавления на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для обновления на уровень хранилища, после чего сохраненную модель
     * возвращает на уровень контроллера
     *
     * @param request несохраненный экземпляр класса {@link UpdateDirectorRequest}
     * @return сохраненный экземпляр класса {@link DirectorDto}
     */
    public DirectorDto update(UpdateDirectorRequest request) {
        log.debug("Запрошено обновление режиссера на уровне сервиса");

        if (!request.hasId()) {
            throw new ValidationException("Id должен быть указан");
        }
        Director existingDirector = directorStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + request.getId() + " не найден"));
        log.debug("Режиссер  id {} найден в хранилище для обновления", existingDirector.getId());

        Director updatedDirector = DirectorMapper.updateDirectorFields(existingDirector, request);

        log.debug("Валидация обновленной модели");
        validate(updatedDirector);
        log.debug("Валидация обновленной модели завершена");

        // Сохраняем изменения
        directorStorage.updateDirector(updatedDirector);

        DirectorDto result = DirectorMapper.mapToDirectorDto(updatedDirector);
        log.debug("Обновленная модель преобразована");

        log.debug("Возврат результатов обновления на уровень контроллера");
        return result;
    }

    public void deleteDirector(Long directorId) {
        log.debug("Запрошено удаление режиссера на уровне сервиса");

        if (directorId == null) {
            throw new ValidationException("Id режиссера не указан");
        }

        log.debug("Передан id режиссера: {}", directorId);

        // Получаем режиссера из хранилища
        Director director = directorStorage.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер и id " + directorId + " не найден"));

        // Удаляем режиссера
        directorStorage.deleteDirector(director.getId());

        log.debug("Возврат результатов удаления на уровень контроллера");
    }

    public void clearDirectors () {
        log.debug("Запрошена очистка хранилища режиссеров");

        // Очищаем хранилище
        directorStorage.clearDirectors();
        log.debug("Все режиссеры удалены");

        log.debug("Возврат результатов очистки на уровень контроллера");
    }

    private void validate(Director director) {
        // Валидация имени
        validateName(director.getName());
    }

    private void validateName(String name) {
        log.debug("Валидация имени");
        if (name == null || name.trim().isBlank()) {
            throw new ValidationException("Имя должно быть указано");
        }
        log.debug("Передано корректное значение name: {}", name);
        log.debug("Валидация имени успешно завершена");
    }
}
