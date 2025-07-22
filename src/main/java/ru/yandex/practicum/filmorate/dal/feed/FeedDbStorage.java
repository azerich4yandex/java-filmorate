package ru.yandex.practicum.filmorate.dal.feed;

import java.sql.Types;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseDbStorage;
import ru.yandex.practicum.filmorate.model.Feed;

@Slf4j
@Component
public class FeedDbStorage extends BaseDbStorage<Feed> implements FeedStorage {

    private static final String GET_FEED_BY_USER_ID_QUERY = """
            SELECT f.EVENT_ID,
            	   f.ENTITY_ID,
            	   f.USER_ID,
            	   f.TIME_FIELD,
            	   f.EVENT_TYPE,
            	   f.OPERATION_TYPE
              FROM FEED f
             WHERE f.USER_ID = :userId
             ORDER BY f.TIME_FIELD
            """;
    private static final String INSERT_FEED_QUERY = """
            INSERT INTO FEED (ENTITY_ID, USER_ID, TIME_FIELD, EVENT_TYPE, OPERATION_TYPE)
            VALUES (:entityId, :userId, :timestamp, :eventType, :operationType)
            """;

    @Autowired
    public FeedDbStorage(NamedParameterJdbcTemplate jdbcTemplate,
                         RowMapper<Feed> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public Collection<Feed> findByUserId(Long userId) {
        log.debug("Запрос всех событий пользователя на уровне хранилища");
        log.debug("Передан идентификатор пользователя: {}", userId);

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT);

        Collection<Feed> result = findMany(GET_FEED_BY_USER_ID_QUERY, parameterSource);
        log.debug("Получена коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public void addFeed(Feed feed) {
        log.debug("Запрос на добавление события на уровне хранилища");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("entityId", feed.getEntityId())
                .addValue("userId", feed.getUserId())
                .addValue("timestamp", feed.getTimestamp())
                .addValue("eventType", feed.getEventType().toString())
                .addValue("operationType", feed.getOperationType().toString());

        boolean isInserted = insertWithOutReturnId(INSERT_FEED_QUERY, parameterSource);
        if (!isInserted) {
            throw new RuntimeException("Не удалось добавить событие в БД");
        } else {
            log.debug("События добавлено в БД");
        }

        log.debug("Возврат результатов добавления на уровень хранилища");
    }
}
