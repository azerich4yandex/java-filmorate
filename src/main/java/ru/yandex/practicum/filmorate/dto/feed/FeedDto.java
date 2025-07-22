package ru.yandex.practicum.filmorate.dto.feed;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeedDto {

    private Long timestamp;
    private Long userId;
    private String eventType;
    private String operation;
    private Long eventId;
    private Long entityId;
}
