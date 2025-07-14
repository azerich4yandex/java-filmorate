package ru.yandex.practicum.filmorate.dal.film;

import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

/**
 * Интерфейс обработки сущностей {@link Film} на уровне хранилища
 */
@Service
public interface FilmStorage {

    /**
     * Метод возвращает коллекцию {@link Film} из хранилища
     *
     * @param size максимальный размер возвращаемой коллекции
     * @param from номер стартового элемента
     * @return коллекция {@link Film}
     */
    Collection<Film> findAll(Integer size, Integer from);


    /**
     * Метод возвращает коллекцию общих для двух пользователей фильмов
     *
     * @param userId идентификатор первого пользователя
     * @param friendId идентификатор второго пользователя
     * @return коллекция {@link Film}
     */
    Collection<Film> findCommon(Long userId, Long friendId);

    /**
     * Метод возвращает коллекцию популярных фильмов
     *
     * @param count размер коллекции
     * @param genreId идентификатор жанра
     * @param year год выпуска
     * @return коллекция популярных фильмов указанного размера
     */
    Collection<Film> findPopular(Integer count, Long genreId, Integer year);

    /**
     * Метод возвращает коллекцию фильмов с таким же жанром
     *
     * @return коллекция фильмов с таким же жанром
     */
    Collection<Film> findByGenreId(Long genreId);

    /**
     * Метод возвращает коллекцию фильмов с таким же рейтингом
     *
     * @param ratingId идентификатор рейтинга
     * @return коллекция фильмов с таким же рейтингом
     */
    Collection<Film> findByRatingId(Long ratingId);

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
    Film createFilm(Film film);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link Film}
     *
     * @param newFilm экземпляр класса {@link Film} для обновления
     */
    void updateFilm(Film newFilm);

    /**
     * Метод добавляет лайк фильму
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    void addLike(Long filmId, Long userId);

    /**
     * Метод удаляет лайк с фильма
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    void removeLike(Long filmId, Long userId);

    /**
     * Метод добавляет жанр фильму
     *
     * @param filmId идентификатор фильма
     * @param genreId идентификатор жанра
     */
    void addGenre(Long filmId, Long genreId);

    /**
     * Метод удаляет жанр из фильма
     *
     * @param filmId идентификатор фильма
     * @param genreId идентификатор жанра
     */
    void removeGenre(Long filmId, Long genreId);

    /**
     * Метод удаляет с фильма переданный рейтинг
     *
     * @param filmId идентификатор фильма
     */
    void removeRating(Long filmId);

    /**
     * Метод удаляет из хранилища переданный экземпляр класса
     *
     * @param filmId идентификатор фильма
     */
    void deleteFilm(Long filmId);

    /**
     * Метод очищает хранилище
     */
    void clearFilms();
}
