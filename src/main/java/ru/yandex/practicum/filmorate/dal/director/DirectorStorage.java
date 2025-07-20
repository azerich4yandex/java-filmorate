package ru.yandex.practicum.filmorate.dal.director;

import java.util.Collection;
import java.util.Optional;
import ru.yandex.practicum.filmorate.model.Director;

/**
 * Интерфейс обработки сущностей {@link Director} на уровне хранилища
 */
public interface DirectorStorage {

    /**
     * Метод возвращает коллекцию {@link Director} из хранилища
     *
     * @param size максимальный размер хранилища
     * @param from номер стартового элемента
     * @return коллекция {@link Director}
     */
    Collection<Director> findAll(Integer size, Integer from);

    /**
     * Метод возвращает коллекцию {@link Director} по идентификатору фильма
     *
     * @param filmId идентификатор фильма
     * @return коллекция {@link Director}
     */
    Collection<Director> findByFilmId(Long filmId);

    /**
     * Метод возвращает экземпляр класса {@link Director} из хранилища на основе переданного идентификатора
     *
     * @param directorId идентификатор режиссера
     * @return найденный экземпляр класса {@link Director}
     */
    Optional<Director> findById(Long directorId);

    /**
     * Метод создает в хранилище переданный экземпляр класса {@link Director}
     *
     * @param director экземпляр класса {@link Director}
     * @return экземпляр класса {@link Director} с заполненными автоматически генерируемыми полями из хранилища
     */
    Director createDirector(Director director);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link Director}
     *
     * @param newDirector экземпляр класса {@link Director} для обновления
     */
    void updateDirector(Director newDirector);

    /**
     * Метод удаляет из хранилища экземпляр класса {@link Director} по его идентификатору
     *
     * @param directorId идентификатор режиссера
     */
    void deleteDirector(Long directorId);

    /**
     * Метод очищает хранилище режиссеров
     *
     */
    void clearDirectors();
}
