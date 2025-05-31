package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ValidationException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Общий обработчик исключений REST-контроллеров
 */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * Обработка исключения {@link NotFoundException}
     *
     * @param e вызванный экземпляр исключения
     * @return набор сведений об ошибке в заданном формате
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(final NotFoundException e) {
        return new ResponseEntity<>(Map.of("error", "Сущность не найдена", "errorMessage", e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка исключения {@link ValidationException}
     *
     * @param e вызванный экземпляр исключения
     * @return набор сведений об ошибке в заданном формате
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(final ValidationException e) {
        return new ResponseEntity<>(Map.of("error", "Ошибка валидации данных", "errorMessage", e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка остальных исключений {@link RuntimeException}
     *
     * @param e вызванный экземпляр исключения
     * @return набор сведений об ошибке в заданном формате
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(final RuntimeException e) {
        return new ResponseEntity<>(Map.of("error", "Произошла непредвиденная ошибка", "errorMessage", e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
