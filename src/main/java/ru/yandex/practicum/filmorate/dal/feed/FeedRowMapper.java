package ru.yandex.practicum.filmorate.dal.feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

@Slf4j
@Component
public class FeedRowMapper implements RowMapper<Feed> {

    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(rs.getLong("event_id"))
                .entityId(rs.getLong("entity_id"))
                .userId(rs.getLong("user_id"))
                .timestamp(rs.getTimestamp("time_field"))
                .eventType(EventTypes.valueOf(rs.getString("event_type")))
                .operationType(OperationTypes.valueOf(rs.getString("operation_type")))
                .build();
    }
}
