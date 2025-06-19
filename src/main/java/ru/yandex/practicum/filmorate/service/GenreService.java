package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.db.dto.create.NewGenreRequest;
import ru.yandex.practicum.filmorate.storage.db.dto.read.GenreDto;
import ru.yandex.practicum.filmorate.storage.db.dto.update.UpdateGenreRequest;
import ru.yandex.practicum.filmorate.storage.db.mappers.GenreMapper;

/**
 * Класс предварительной обработки и валидации сущностей {@link Genre} на уровне сервиса
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;
    private final FilmStorage filmStorage;

    /**
     * Метод возвращает коллекцию {@link GenreDto}
     *
     * @param size максимальный размер коллекции
     * @param from номер стартового элемента
     * @return результирующая коллекция
     */
    public Collection<GenreDto> findAll(Integer size, Integer from) {
        log.debug("Запрос всех жанров на уровне сервиса");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Genre> searchResult = genreStorage.findAll(size, from);
        log.debug("На уровень сервиса вернулась коллекция размером {}", searchResult.size());

        Collection<GenreDto> result = searchResult.stream().map(GenreMapper::mapToGenreDto).toList();
        log.debug("Найденная коллекция преобразована. Размер коллекции после преобразования: {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link GenreDto}, найденный по идентификатору
     *
     * @param genreId идентификатор жанра
     * @return экземпляр класса {@link GenreDto}
     * @throws ValidationException если передан пустой genreId
     * @throws NotFoundException если экземпляр не найден
     */
    public GenreDto findById(Long genreId) throws ValidationException, NotFoundException {
        log.debug("Поиск жанра в id на уровне сервиса");

        if (genreId == null) {
            throw new ValidationException("Передан пустой genreId");
        }
        log.debug("Передан id жанра: {}", genreId);

        Genre searchResult = genreStorage.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + " не найден"));
        log.debug("Жанр с id {} найден в хранилище", searchResult.getId());

        GenreDto result = GenreMapper.mapToGenreDto(searchResult);
        log.debug("Найденный жанр с id {} преобразован", result.getId());

        log.debug("Возврат результата поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для сохранения на уровень хранилища, после чего сохранённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохранённый экземпляр класса {@link NewGenreRequest}
     * @return сохранённый экземпляр класса {@link GenreDto}
     */
    public GenreDto create(NewGenreRequest request) {
        log.debug("Создание жанра на уровне сервиса");

        Genre genre = GenreMapper.mapToGenre(request);
        log.debug("Переданная модель преобразована");

        log.debug("Валидация переданной модели");
        validate(genre);
        log.debug("Валидация модели завершена");

        genre = genreStorage.createGenre(genre);

        GenreDto result = GenreMapper.mapToGenreDto(genre);
        log.debug("Сохранённая модель преобразована");

        log.debug("Возврат результатов добавления на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет полученную модель и передает для обновления на уровень хранилища, после чего обновлённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохранённый экземпляр класса {@link UpdateGenreRequest}
     * @return сохранённый экземпляр класса {@link GenreDto}
     * @throws ValidationException при ошибках валидации
     */
    public GenreDto update(UpdateGenreRequest request) throws ValidationException {
        log.debug("Обновление жанра на уровне сервиса");

        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Передан жанр с id: {}", request.getId());

        // Получаем жанр из хранилища
        Genre existingGenre = genreStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Жанр с id " + request.getId() + " не найден в хранилище"));
        log.debug("Жанр с id {} найден  в хранилище", existingGenre.getId());

        Genre updatedGenre = GenreMapper.updateGenreFields(existingGenre, request);

        log.debug("Валидация обновлённой модели");
        validate(updatedGenre);
        log.debug("Валидация обновлённой модели завершена");

        // Сохраняем изменения
        genreStorage.updateGenre(updatedGenre);

        GenreDto result = GenreMapper.mapToGenreDto(updatedGenre);
        log.debug("Обновленная модель преобразована");

        log.debug("Возврат результатов обновления на уровень контроллера");
        return result;
    }

    /**
     * Метод удаляет экземпляр класса {@link Genre} по переданному id
     *
     * @param genreId идентификатор жанра
     * @throws ValidationException при ошибках валидации
     * @throws NotFoundException если удаляемый жанр не найден в хранилище
     */
    public void deleteGenre(Long genreId) throws ValidationException, NotFoundException {
        log.debug("Удаление жанра на уровне сервиса");

        if (genreId == null) {
            throw new ValidationException("Передан пустой id");
        }
        log.debug("Передан id удаляемого жанра: {}", genreId);

        // Получаем жанр из хранилища
        Genre genre = genreStorage.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + " не найден"));
        log.debug("В хранилище найден жанр с id {}", genre.getId());

        log.debug("Получаем фильмы с таким же жанром");
        Collection<Film> films = filmStorage.findByGenreId(genre.getId());
        log.debug("На уровень сервиса вернулась коллекция фильмов размером {}", films.size());

        // Удаляем из коллекции жанров найденных фильмов искомый жанр
        if (!films.isEmpty()) {
            log.debug("Жанр используется другими фильмами");

            for (Film film : films) {
                // Удаляем жанр из фильма
                filmStorage.removeGenre(film.getId(), genre.getId());
            }
        } else {
            log.debug("Жанр не используется другими фильмами");
        }

        // Удаляем жанр
        genreStorage.deleteGenre(genre.getId());

        log.debug("Возврат результата удаления на уровень контроллера");
    }

    public void clearGenres() {
        log.debug("Очистка жанров на уровне сервиса");

        genreStorage.clearGenres();

        log.debug("Возврат результатов очистки на уровень контроллера");
    }

    private void validate(Genre genre) {
        // Валидация наименования
        validateName(genre);

    }

    private void validateName(Genre genre) throws ValidationException {
        log.debug("Запускаем валидацию наименования");

        // Наименование не должно быть пустым
        if (genre.getName() == null || genre.getName().isBlank()) {
            throw new ValidationException("Наименование не может быть пустым");
        }

        // Наименование не должно быть ранее использовано
        boolean exists = genreStorage.isNameAlreadyUsed(genre);

        if (exists) {
            throw new ValidationException("Наименование " + genre.getName() + " уже используется");
        }

        log.debug("Передано корректное значение name: {}", genre.getName());
        log.debug("Валидация наименования завершена");
    }
}
