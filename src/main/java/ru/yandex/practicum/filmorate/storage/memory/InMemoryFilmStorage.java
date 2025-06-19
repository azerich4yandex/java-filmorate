package ru.yandex.practicum.filmorate.storage.memory;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

/**
 * Класс обработки сущностей {@link Film} на уровне хранилища. Реализация хранения данных в оперативной памяти.
 */
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final MpaStorage mpaStorage;
    private final HashMap<Long, Film> films = new HashMap<>();
    private long generatedId;

    @Autowired
    public InMemoryFilmStorage(MpaStorage mpaStorage) {
        log.debug("Будет использована реализация хранения фильмов в памяти приложения");

        this.mpaStorage = mpaStorage;
    }

    @Override
    public Collection<Film> findAll(Integer size, Integer from) {
        log.debug("Запрос всех фильмов на уровне хранилища");
        log.debug("Размер коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Film> result = films.values().stream()
                .skip(from)
                .limit(size)
                .toList();
        log.debug("На уровне хранилища получена коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findPopular(Integer count) {
        log.debug("Запрос топ фильмов на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", count);

        Collection<Film> result = films.values().stream()
                .filter(film -> !film.getLikes().isEmpty())
                .sorted(Comparator.comparing((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
        log.debug("Получена коллекция размером {}", result.size());

        log.debug("Возврат результатов на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findByGenreId(Long genreId) {
        log.debug("Запрос списка фильмов по жанру");
        log.debug("Идентификатор запрашиваемого жанра: {}", genreId);

        Collection<Film> result = films.values().stream()
                .filter(film -> film.getGenres().contains(genreId))
                .toList();
        log.debug("Получена коллекция фильмов размером {}", result.size());

        log.debug("Возврат результатов поиска по жанру на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Film> findByRatingId(Long ratingId) {
        log.debug("Запрос списка фильмов по рейтингу");
        log.debug("Идентификатор запрашиваемого рейтинга: {}", ratingId);

        Collection<Film> result = films.values().stream()
                .filter(film -> film.getMpa().getId().equals(ratingId))
                .toList();
        log.debug("Получена коллекция фильмов по жанру размером {}", result.size());

        log.debug("Возврат результатов поиска по рейтингу на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        log.debug("Поиск фильма по id на уровне хранилища");
        log.debug("Идентификатор запрашиваемого фильма: {}", filmId);

        Film film = films.get(filmId);
        log.debug("Фильм с id {} {} в хранилище", filmId, film == null ? "не найден" : "найден");

        log.debug("Возврат результата поиска на уровень сервиса");
        return Optional.ofNullable(film);
    }

    @Override
    public Film createFilm(Film film) {
        log.debug("Создание фильма на уровне хранилища");

        Long nextId = getNextId();
        log.debug("Сгенерировано значение {}", nextId);

        film.setId(nextId);
        log.debug("Значение присвоено id присвоено фильму");

        save(film);

        log.debug("Возврат результатов создания на уровень сервиса");
        return film;
    }

    @Override
    public void updateFilm(Film newFilm) {
        log.debug("Изменение фильма на уровне хранилища");

        save(newFilm);

        log.debug("Возврат результатов изменения на уровень сервиса");
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка на уровне хранилища");
        log.debug("Идентификатор фильма: {}", filmId);
        log.debug("Идентификатор пользователя: {}", userId);

        // Получаем фильм из хранилища
        Film film = films.get(filmId);
        log.debug("В хранилище найден фильм с id {}", film.getId());

        // Добавляем идентификатор пользователя к лайкам фильма
        film.getLikes().add(userId);
        log.debug("Фильму с id {} добавлен лайк от пользователя с id {}", film.getId(), userId);

        // Сохраняем изменения фильма
        save(film);

        log.debug("Возврат результат добавления лайка на уровень сервиса");
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка на уровне хранилища");
        log.debug("Идентификатор  фильма: {}", filmId);
        log.debug("Идентификатор  пользователя: {}", userId);

        // Получаем фильм из хранилища
        Film film = films.get(filmId);
        log.debug("В хранилище найден фильм с идентификатором {}", film.getId());

        // Удаляем идентификатор пользователя из лайков фильма
        film.getLikes().remove(userId);
        log.debug("У фильма с id {} удалён лайк от пользователя с id {}", film.getId(), userId);

        // Сохраняем изменение фильма
        save(film);

        log.debug("Возврат результата удаления лайка на уровень сервиса");
    }

    @Override
    public void addGenre(Long filmId, Long genreId) {
        log.debug("Добавление жанра фильму на уровне хранилища");
        log.debug("Переданный идентификатор фильма: {}", filmId);
        log.debug("Переданный идентификатор жанра: {}", genreId);

        // Получаем фильм из хранилища
        Film film = films.get(filmId);
        log.debug("В хранилище найден фильм  с id {}", film.getId());

        // Устанавливаем жанр фильму
        film.getGenres().add(genreId);

        // Сохраняем изменение фильма
        save(film);

        log.debug("Возврат результатов добавления жанра на уровень сервиса");
    }

    @Override
    public void removeGenre(Long filmId, Long genreId) {
        log.debug("Удаление жанра из фильма на уровне хранилища");
        log.debug("Переданный идентификатор изменяемого фильма: {}", filmId);
        log.debug("Переданный идентификатор удаляемого жанра: {}", genreId);

        // Получаем фильм из хранилища
        Film film = films.get(filmId);
        log.debug("В хранилище найден фильм с  идентификатором {}", film.getId());

        // Удаляем жанр из коллекции
        film.getGenres().remove(genreId);

        // Сохраняем изменение фильма
        save(film);

        log.debug("Возврат результата удаления жанра на уровень сервиса");
    }

    @Override
    public void addRating(Long filmId, Long ratingId) {
        log.debug("Установка рейтинга фильму на уровне хранилища");
        log.debug("Идентификатор переданного фильма: {}", filmId);
        log.debug("Идентификатор переданного жанра: {}", ratingId);

        // Получаем фильм из хранилища
        Film film = films.get(filmId);
        log.debug("Из хранилища получен фильм с id {}", film.getId());

        // Получаем рейтинг из хранилища
        Mpa rating = mpaStorage.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + ratingId + " не найден"));
        log.debug("Из хранилища получен рейтинг с id {}", rating.getId());

        // Устанавливаем рейтинг фильму
        film.setMpa(rating);

        // Сохраняем изменение фильма
        save(film);

        log.debug("Возврат результатов установки рейтинга на уровень сервиса");
    }

    @Override
    public void removeRating(Long filmId) {
        log.debug("Удаление рейтинга с фильма на уровне хранилища");
        log.debug("Передан id изменяемого фильма: {}", filmId);

        // Получаем фильм из хранилища
        Film film = films.get(filmId);
        log.debug("В хранилище  найден фильм с идентификатором {}", film.getId());

        // Устанавливаем рейтинг фильму
        film.setMpa(null);

        // Сохраняем изменение фильма
        save(film);

        log.debug("Возврат результатов удаления рейтинга на уровень сервиса");
    }

    public void save(Film film) {
        log.debug("Сохранение в хранилище");

        films.put(film.getId(), film);

        log.debug("Сохранение в хранилище завершено");
    }

    @Override
    public void deleteFilm(Long filmId) {
        log.debug("Удаление фильма на уровне хранилища");
        log.debug("Передан id фильма: {}", filmId);

        films.remove(filmId);
        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearFilms() {
        log.debug("Очистка хранилища фильмов");

        films.clear();

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    private Long getNextId() {
        log.debug("Генерация нового идентификатора для фильма");
        return ++generatedId;
    }
}
