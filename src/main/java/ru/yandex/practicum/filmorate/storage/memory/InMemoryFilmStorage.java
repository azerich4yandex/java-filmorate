package ru.yandex.practicum.filmorate.storage.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

/**
 * Класс обработки сущностей {@link Film} на уровне хранилища. Реализация хранения данных в оперативной памяти.
 */
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final HashMap<Long, Film> films = new HashMap<>();
    private long generatedId;

    @Override
    public Collection<Film> findAll() {
        log.debug("Запрос всех фильмов на уровне хранилища");

        Collection<Film> result = films.values();

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        log.debug("Поиск фильма по filmId на уровне хранилища");
        Film film = films.get(filmId);

        log.debug("Фильм с filmId {} {} в хранилище", filmId, film == null ? "не найден" : "найден");

        log.debug("Возврат результата поиска на уровень сервиса");
        return Optional.ofNullable(film);
    }

    @Override
    public Film create(Film film) {
        log.debug("Создание фильма на уровне хранилища");

        film.setId(getNextId());

        save(film);

        log.debug("Возврат результатов создания на уровень сервиса");
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.debug("Изменение фильма на уровне хранилища");

        save(newFilm);

        log.debug("Возврат результатов изменения на уровень сервиса");
        return newFilm;
    }

    @Override
    public void save(Film film) {
        log.debug("Помещение изменений в хранилище");
        films.put(film.getId(), film);
        log.debug("Помещение изменений в хранилище завершено");
    }

    @Override
    public void delete(Long filmId) {
        log.debug("Удаление фильма на уровне хранилища");

        films.remove(filmId);

        log.debug("Удаление фильма завершено");
    }

    @Override
    public void clear() {
        log.debug("Очистка хранилища фильмов");

        films.clear();

        log.debug("Очистка хранилища завершена");
    }

    private Long getNextId() {
        log.debug("Генерация нового идентификатора для фильма");
        return ++generatedId;
    }
}
