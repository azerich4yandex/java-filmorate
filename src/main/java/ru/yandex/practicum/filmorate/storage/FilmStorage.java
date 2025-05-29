package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Optional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

/**
 * Интерфейс обработки сущностей {@link Film} на уровне хранилища
 */
public interface FilmStorage {

    /**
     * Метод возвращает коллекцию {@link Film} из хранилища
     *
     * @return коллекция {@link Film}
     */
    Collection<Film> findAll();


    /**
     * Метод возвращает экземпляр класса {@link Film} из хранилища на основе переданного идентификатора
     *
     * @param filmId идентификатор фильма
     * @return найденный экземпляр класса {@link Film}
     * @throws NotFoundException если фильм не найден
     */
    Optional<Film> findById(Long filmId);

    /**
     * Метод создаёт в хранилище переданный экземпляр класса {@link Film}
     *
     * @param film экземпляр класса {@link Film}
     * @return экземпляр класса {@link Film} с заполненными автоматически генерируемыми полями из хранилища
     */
    Film create(Film film);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link Film}
     *
     * @param newFilm экземпляр класса {@link Film} для обновления
     * @return обновленный экземпляр класса {@link Film} из хранилища
     */
    Film update(Film newFilm);

    /**
     * Метод сохраняет в хранилище переданный экземпляр класса {@link Film}
     *
     * @param film экземпляр класса {@link Film}
     */
    void save(Film film);


    /**
     * Метод удаляет из хранилища переданный экземпляр класса
     *
     * @param filmId идентификатор фильма
     */
    void delete(Long filmId);

    /**
     * Метод очищает хранилище
     */
    void clear();
}
