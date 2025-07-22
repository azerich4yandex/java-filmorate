package ru.yandex.practicum.filmorate.dto.review;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateReviewRequest {

    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;

    public boolean hasId() {
        return reviewId != null && reviewId > 0;
    }

    public boolean hasContent() {
        return !(content == null || content.isBlank());
    }

    public boolean hasPositive() {
        return isPositive != null;
    }
}
