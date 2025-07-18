package ru.yandex.practicum.filmorate.dto.review;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NewReviewRequest {

    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
}
