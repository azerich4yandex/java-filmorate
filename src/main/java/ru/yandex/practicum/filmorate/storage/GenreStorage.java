package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;

/**
 * Интерфейс обработки сущностей {@link Genre} на уровне хранилища
 */
@Service
public interface GenreStorage {

    /**
     * Метод возвращает коллекцию {@link Genre} из хранилища
     *
     * @return коллекция {@link Genre}
     */
    Collection<Genre> findAll(Integer size, Integer from);

    /**
     * Метод возвращает коллекцию жанров, связанных с фильмом
     *
     * @param filmId  идентификатор фильма
     * @return коллекция связанных жанров
     */
    Collection<Genre> findByFilmId(Long filmId);

    /**
     * Метод возвращает экземпляр класса {@link Genre} из хранилища на основе переданного идентификатора
     *
     * @param genreId идентификатор жанра
     * @return найденный экземпляр класса {@link Genre}
     */
    Optional<Genre> findById(Long genreId);

    /**
     * Метод создаёт в хранилище переданный экземпляр класса {@link Genre}
     *
     * @param genre экземпляр класса {@link Genre}
     * @return экземпляр класса {@link Genre} с заполненными автоматически генерируемыми полями из хранилища
     */
    Genre createGenre(Genre genre);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link Genre}
     *
     * @param newGenre экземпляр класса {@link Genre} для обновления
     */
    void updateGenre(Genre newGenre);

    /**
     * Метод удаляет экземпляр класса {@link Genre}, найденный в хранилище по id
     *
     * @param genreId идентификатор жанра
     */
    void deleteGenre(Long genreId);

    /**
     * Метод очищает хранилище жанров
     */
    void clearGenres();

    /**
     * Метод проверяет использование наименование в других жанрах
     * @param genre экземпляр
     * @return результат проверки
     */
    boolean isNameAlreadyUsed(Genre genre);
}
