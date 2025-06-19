package ru.yandex.practicum.filmorate.storage.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

@Slf4j
@Component
public class InMemoryGenreStorage implements GenreStorage {

    private final HashMap<Long, Genre> genres = new HashMap<>();
    private final FilmStorage filmStorage;
    private long generatedId;

    @Autowired
    public InMemoryGenreStorage(FilmStorage filmStorage) {
        log.debug("Будет использована реализация хранения жанров в памяти приложения");

        this.filmStorage = filmStorage;
    }

    @Override
    public Collection<Genre> findAll(Integer size, Integer from) {
        log.debug("Запрос всех жанров на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<Genre> result = genres.values().stream()
                .skip(from)
                .limit(size)
                .toList();
        log.debug("Получена коллекция всех жанров размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<Genre> findByFilmId(Long filmId) {
        log.debug("Запрос жанров по идентификатору фильма на уровне хранилища");
        log.debug("Идентификатор запрашиваемого фильма: {}", filmId);

        Optional<Film> filmOpt = filmStorage.findById(filmId);

        Collection<Genre> result;

        if (filmOpt.isPresent()) {
            Film film = filmOpt.get();
            result = genres.values().stream().filter(genre -> film.getGenres().contains(genre.getId())).toList();
        } else {
            result = new ArrayList<>();
        }
        log.debug("Получена коллекция размером {}", result.size());

        log.debug("Возврат результатов на уровень сервиса");
        return result;
    }

    @Override
    public Optional<Genre> findById(Long genreId) {
        log.debug("Поиск жанра по id на уровне хранилища");
        log.debug("Идентификатор запрашиваемого жанра: {}", genreId);

        Genre genre = genres.get(genreId);
        log.debug("Жанр с id {} {} в хранилище", genreId, genre == null ? "не найден" : "найден");

        log.debug("Возврат результата поиска на уровень сервиса");
        return Optional.ofNullable(genres.get(genreId));
    }

    @Override
    public Genre createGenre(Genre genre) {
        log.debug("Создание жанра на уровне хранилища");

        Long nextId = getNextId();
        log.debug("Сгенерировано значение {}", nextId);

        genre.setId(nextId);
        log.debug("Значение присвоено id присвоено жанру");

        save(genre);

        log.debug("Возврат результатов создания на уровень сервиса");
        return genre;
    }

    public void save(Genre genre) {
        log.debug("Сохранение жанра в хранилище");

        genres.put(genre.getId(), genre);

        log.debug("Сохранение жанра завершено");
    }

    @Override
    public void updateGenre(Genre newGenre) {
        log.debug("Изменение жанра на уровне хранилища");

        save(newGenre);

        log.debug("Возврат результатов изменения на уровень сервиса");
    }

    @Override
    public void deleteGenre(Long genreId) {
        log.debug("Удаление жанра на уровне хранилища");
        log.debug("Передан id жанра: {}", genreId);

        genres.remove(genreId);
        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearGenres() {
        log.debug("Очистка хранилища жанров");

        genres.clear();

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    @Override
    public boolean isNameAlreadyUsed(Genre genre) {
        log.debug("Проверка на использование наименования жанра {} другими жанрами", genre.getName());

        return genres.values().stream().anyMatch(
                existingGenre -> !existingGenre.getId().equals(genre.getId()) && existingGenre.getName()
                        .equalsIgnoreCase(genre.getName()));
    }

    private long getNextId() {
        log.debug("Генерация нового идентификатора для жанра");

        return ++generatedId;
    }
}
