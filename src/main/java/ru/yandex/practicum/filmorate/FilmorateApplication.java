package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FilmorateApplication {

    /**
     * Метод - точка входа в приложение
     * @param args параметры командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
    }
}
