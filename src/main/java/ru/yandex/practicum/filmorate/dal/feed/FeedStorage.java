package ru.yandex.practicum.filmorate.dal.feed;

import java.util.Collection;
import ru.yandex.practicum.filmorate.model.Feed;

/**
 * Интерфейс обработки сущностей {@link Feed}
 */
public interface FeedStorage {

    /**
     * Метод возвращает коллекцию {@link Feed} по идентификатору пользователя из хранилища
     *
     * @param userId идентификатор пользователя
     * @return коллекция {@link Feed}
     */
    Collection<Feed> findByUserId(Long userId);

    /**
     * Метод добавляет событие в ленту пользователя
     *
     * @param feed экземпляр класса {@link Feed}
     */
    void addFeed(Feed feed);
}
