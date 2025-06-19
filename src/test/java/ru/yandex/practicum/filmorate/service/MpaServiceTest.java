package ru.yandex.practicum.filmorate.service;

import java.util.Arrays;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Работа с хранилищем рейтингов")
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class MpaServiceTest {

    private final MpaService mpaService;

    @DisplayName("Получение списка рейтингов")
    @Test
    public void getAllMpaTest() {
        Collection<MpaDto> mpa = mpaService.findAll(10, 0);
        assertThat(mpa)
                .extracting(MpaDto::getName)
                .containsAll(Arrays.asList("G", "PG", "PG-13", "R", "NC-17"));
    }

    @DisplayName("Получение рейтинга по идентификатору")
    @Test
    public void getMpaByIdTest() {
        MpaDto rating = mpaService.findById(4L);
        assertEquals("R", rating.getName());
    }
}
