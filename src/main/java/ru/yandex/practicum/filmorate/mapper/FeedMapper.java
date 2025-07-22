package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.feed.FeedDto;
import ru.yandex.practicum.filmorate.model.Feed;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeedMapper {

    public static FeedDto mapToFeedDto(Feed feed) {
        return FeedDto.builder()
                .timestamp(feed.getTimestamp().getTime())
                .userId(feed.getUserId())
                .eventType(feed.getEventType().name())
                .operation(feed.getOperationType().name())
                .eventId(feed.getEventId())
                .entityId(feed.getEntityId())
                .build();
    }
}
