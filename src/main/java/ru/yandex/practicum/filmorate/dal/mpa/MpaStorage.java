package ru.yandex.practicum.filmorate.dal.mpa;

import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;

/**
 * Интерфейс обработки сущностей {@link Mpa} на уровне хранилища
 */
@Service
public interface MpaStorage {

    /**
     * Метод возвращает коллекцию {@link Mpa} из хранилища
     *
     * @param size максимальный размер коллекции
     * @param from номер стартового элемента
     * @return коллекция {@link Mpa}
     */
    Collection<Mpa> findAll(Integer size, Integer from);

    /**
     * Метод возвращает экземпляр класса {@link Mpa} из хранилища на основе переданного идентификатора
     *
     * @param ratingId идентификатор рейтинга
     * @return найденный экземпляр класса {@link Mpa}
     */
    Optional<Mpa> findById(Long ratingId);

    /**
     * Метод создаёт в хранилище переданный экземпляр класса {@link Mpa}
     *
     * @param rating экземпляр класса {@link Mpa}
     * @return экземпляр класса {@link Mpa} с заполненными автоматически генерируемыми полями из хранилища
     */
    Mpa createRating(Mpa rating);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link Mpa}
     *
     * @param newRating экземпляр класса {@link Mpa} для обновления
     * @return обновленный экземпляр класса {@link Mpa} из хранилища
     */
    Mpa updateRating(Mpa newRating);

    /**
     * Метод удаляет экземпляр класса {@link Mpa} из хранилища
     *
     * @param ratingId идентификатор рейтинга
     */
    void deleteRating(Long ratingId);

    /**
     * Метод очищает хранилище рейтингов
     */
    void clearRatings();

    /**
     * Метод проверяет использование наименования другими рейтингами
     *
     * @param rating экземпляр класс {@link Mpa}
     */
    boolean isNameAlreadyUser(Mpa rating);
}
