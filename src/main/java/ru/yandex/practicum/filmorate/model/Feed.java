package ru.yandex.practicum.filmorate.model;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

/**
 * Событие
 */
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "eventId")
@NoArgsConstructor
public class Feed {

    /**
     * Идентификатор сущности
     */
    private Long eventId;

    /**
     * Идентификатор обработанной сущности
     */
    private Long entityId;

    /**
     * Идентификатор пользователя
     */
    private Long userId;

    /**
     * Метка времени
     */
    private Timestamp timestamp;

    /**
     * Тип события
     */
    private EventTypes eventType;

    /**
     * Тип операции
     */
    private OperationTypes operationType;
}
