package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FilmControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void init() {
        film1 = Film.builder().name("A").description("a".repeat(200)).releaseDate(LocalDate.now().minusDays(1))
                .duration(120).build();
        film2 = Film.builder().name("B").description("b".repeat(200)).releaseDate(LocalDate.now().minusDays(2))
                .duration(180).build();
        film3 = Film.builder().name("C").description("c".repeat(200)).releaseDate(LocalDate.now().minusDays(3))
                .duration(240).build();

        user1 = User.builder().login("a").name("A").email("a@mail.ru").birthday(LocalDate.now().minusYears(1)).build();
        user2 = User.builder().login("b").name("B").email("b@mail.ru").birthday(LocalDate.now().minusYears(2)).build();
        user3 = User.builder().login("c").name("C").email("c@mail.ru").birthday(LocalDate.now().minusYears(3)).build();

    }

    @AfterEach
    void halt() {
        // Очищаем хранилище
        filmController.clearFilms();
        userController.clearUsers();
    }

    @DisplayName("Добавление фильма без вызова исключений")
    @Test
    void shouldCreateOrUpdateFilmWithoutThrowingExceptions() {
        // Получаем список фильмов до добавления
        ResponseEntity<Collection<Film>> responseFilms = filmController.findAll();
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        Collection<Film> films = responseFilms.getBody();
        assertNotNull(films);
        assertTrue(films.isEmpty());

        // Добавляем фильм
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        film1 = responseFilm.getBody();
        assertNotNull(film1);
        assertNotNull(film1.getId());

        // Устанавливаем пустую дату релиза
        LocalDate tempDate = film1.getReleaseDate();
        film1.setReleaseDate(null);
        responseFilm = filmController.update(film1);
        assertEquals(HttpStatus.OK, responseFilm.getStatusCode());
        film1 = responseFilm.getBody();
        assertNotNull(film1);
        assertNull(film1.getReleaseDate());
        film1.setReleaseDate(tempDate);

        // Устанавливаем пустое значение длительности
        film1.setDuration(null);
        responseFilm = filmController.update(film1);
        assertEquals(HttpStatus.OK, responseFilm.getStatusCode());
        film1 = responseFilm.getBody();
        assertNotNull(film1);
        assertNull(film1.getDuration());
    }

    @DisplayName("Получение фильма без вызова исключений")
    @Test
    void shouldReturnFilmWithoutThrowingExceptions() {
        // Добавляем фильм
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());

        responseFilm = filmController.findById(film1.getId());
        assertEquals(HttpStatus.OK, responseFilm.getStatusCode());
    }

    @DisplayName("Вызов исключений при получении фильма по id")
    @Test
    void shouldThrowNotFoundOrValidationExceptionsWhenFindById() {
        // Получаем фильма по отрицательному id
        assertThrows(NotFoundException.class, () -> filmController.findById(-1000L));

        // Получаем фильм по неизвестному id
        assertThrows(NotFoundException.class, () -> filmController.findById(1000L));

        // Получаем фильм по пустому id
        assertThrows(ValidationException.class, () -> filmController.findById(null));
    }

    @DisplayName("Получение коллекции фильмов без вызова исключений")
    @Test
    void shouldReturnNotNullCollectionWhenFindAll() {
        // Получаем пустую коллекцию
        ResponseEntity<Collection<Film>> responseFilms = filmController.findAll();
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        Collection<Film> films = responseFilms.getBody();
        assertNotNull(films);
        assertTrue(films.isEmpty());

        // Добавляем первый фильм
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());

        // Добавляем второй фильм
        responseFilm = filmController.create(film2);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());

        // Добавляем третий фильм
        responseFilm = filmController.create(film3);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());

        // Получаем список фильмов после добавления
        responseFilms = filmController.findAll();
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        films = responseFilms.getBody();
        assertNotNull(films);
        assertFalse(films.isEmpty());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));
        assertTrue(films.contains(film3));
    }

    @DisplayName("Обновление фильма с вызовом исключений")
    @Test
    void shouldThrowValidationOrNotFoundExceptionsWhenUpdate() {
        // Добавляем фильм
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());

        // Устанавливаем пустое наименование фильма
        String tempString = film1.getName();
        film1.setName(null);
        assertThrows(ValidationException.class, () -> filmController.update(film1));
        film1.setName(tempString);

        // Устанавливаем пустое описание фильма
        tempString = film1.getDescription();
        film1.setDescription(null);
        assertThrows(ValidationException.class, () -> filmController.update(film1));

        // Устанавливаем длинное описание фильма
        film1.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.update(film1));
        film1.setDescription(tempString);

        // Устанавливаем дату релиза раньше минимальной даты
        LocalDate tempDate = film1.getReleaseDate();
        film1.setReleaseDate(LocalDate.of(1895, 12, 28).minusDays(1));
        assertThrows(ValidationException.class, () -> filmController.update(film1));
        film1.setReleaseDate(tempDate);

        // Устанавливаем отрицательное значение длительности
        Integer tempInteger = film1.getDuration();
        film1.setDuration(-1 * tempInteger);
        assertThrows(ValidationException.class, () -> filmController.update(film1));
        film1.setDuration(tempInteger);

        // Устанавливаем неизвестный id
        film1.setId(film1.getId() * 100);
        assertThrows(NotFoundException.class, () -> filmController.update(film1));
    }


    @DisplayName("Удаление фильма без вызова исключений и вызов исключений при удалении")
    @Test
    void shouldDeleteOrThrowValidationOrNotFoundExceptions() {
        // Добавляем фильм
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        Long tempLong = film1.getId();

        // Удаляем фильм
        ResponseEntity<Void> responseVoid = filmController.deleteFilm(film1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Повторно удаляем тот же фильм
        assertThrows(NotFoundException.class, () -> filmController.deleteFilm(tempLong));

        // Удалям фильм с id == null
        assertThrows(ValidationException.class, () -> filmController.deleteFilm(null));

        // Удаляем фильм с несуществующим id
        assertThrows(NotFoundException.class, () -> filmController.deleteFilm(tempLong * 100));

        // Удаляем фильм с отрицательным id
        assertThrows(NotFoundException.class, () -> filmController.deleteFilm(-1 * tempLong));
    }

    @DisplayName("Очистка хранилища фильмов без вызова исключений")
    @Test
    void shouldClearStorageWithoutThrowingException() {
        // Добавляем несколько фильмов
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        assertNotNull(film1);
        assertNotNull(film1.getId());

        responseFilm = filmController.create(film2);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        assertNotNull(film2);
        assertNotNull(film2.getId());
        assertNotEquals(film1.getId(), film2.getId());

        // Получаем список фильмов из хранилища
        ResponseEntity<Collection<Film>> responseFilms = filmController.findAll();
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        Collection<Film> films = responseFilms.getBody();
        assertNotNull(films);
        assertFalse(films.isEmpty());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));

        // Очищаем хранилище
        ResponseEntity<Void> responseVoid = filmController.clearFilms();
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем список фильм из хранилища после удаления
        responseFilms = filmController.findAll();
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        films = responseFilms.getBody();
        assertNotNull(films);
        assertTrue(films.isEmpty());
    }

    @DisplayName("Добавление/удаление/получение лайков без вызова исключений")
    @Test
    void shouldAddOrRemoveLikesWithOutThrowingException() {
        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user1 = responseUser.getBody();
        assertNotNull(user1);
        assertNotNull((user1.getId()));

        // Добавляем второго пользователя
        responseUser = userController.create(user2);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user2 = responseUser.getBody();
        assertNotNull(user2);
        assertNotNull((user2.getId()));

        // Добавляем третьего пользователя
        responseUser = userController.create(user3);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user3 = responseUser.getBody();
        assertNotNull(user3);
        assertNotNull((user3.getId()));

        // Добавляем первый фильм
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        film1 = responseFilm.getBody();
        assertNotNull(film1);
        assertNotNull(film1.getId());

        // Добавляем второй фильм
        responseFilm = filmController.create(film2);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        film2 = responseFilm.getBody();
        assertNotNull(film2);
        assertNotNull(film2.getId());

        // Добавляем третий фильм
        responseFilm = filmController.create(film3);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        film3 = responseFilm.getBody();
        assertNotNull(film3);
        assertNotNull(film3.getId());

        // Добавляем лайк первому фильму от первого пользователя
        ResponseEntity<Void> responseVoid = filmController.addLike(film1.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Добавляем лайк первому фильму от второго пользователя
        responseVoid = filmController.addLike(film1.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Добавляем лайк первому фильму от третьего пользователя
        responseVoid = filmController.addLike(film1.getId(), user3.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Добавляем лайк второму фильму от первого пользователя
        responseVoid = filmController.addLike(film2.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем топ-2 фильмов
        ResponseEntity<Collection<Film>> responseFilms = filmController.findPopular(2);
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        Collection<Film> films = responseFilms.getBody();
        assertNotNull(films);
        assertFalse(films.isEmpty());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));
        assertFalse(films.contains(film3));

        // Получаем топ-1000 фильмов
        responseFilms = filmController.findPopular(1000);
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        films = responseFilms.getBody();
        assertNotNull(films);
        assertFalse(films.isEmpty());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));
        assertFalse(films.contains(film3));

        // Получаем топ-null фильмов
        responseFilms = filmController.findPopular(null);
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        films = responseFilms.getBody();
        assertNotNull(films);
        assertFalse(films.isEmpty());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));
        assertFalse(films.contains(film3));
        assertEquals(2, films.size());

        // Удаляем лайк второго фильма от первого пользователя
        responseVoid = filmController.removeLike(film2.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Удаляем лайк второго фильма от второго пользователя
        responseVoid = filmController.removeLike(film2.getId(), user2.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Получаем топ-10 фильмов
        responseFilms = filmController.findPopular(10);
        assertEquals(HttpStatus.OK, responseFilms.getStatusCode());
        films = responseFilms.getBody();
        assertNotNull(films);
        assertFalse(films.isEmpty());
        assertTrue(films.contains(film1));
        assertFalse(films.contains(film2));
        assertFalse(films.contains(film3));
        assertEquals(1, films.size());
    }

    @DisplayName("Добавление/удаление лайков с вызовом исключений")
    @Test
    void shouldThrowNotFoundOrValidationExceptionWhenAddingOrRemovingLike() {
        // Добавляем первый фильм
        ResponseEntity<Film> responseFilm = filmController.create(film1);
        assertEquals(HttpStatus.CREATED, responseFilm.getStatusCode());
        film1 = responseFilm.getBody();
        assertNotNull(film1);
        assertNotNull(film1.getId());

        // Добавляем лайк неизвестного пользователя первому фильму
        assertThrows(NotFoundException.class, () -> filmController.addLike(film1.getId(), 1L));

        // Добавляем лайк null пользователя первому фильму
        assertThrows(ValidationException.class, () -> filmController.addLike(film1.getId(), null));

        // Добавляем лайк отрицательного пользователя первому фильму
        assertThrows(NotFoundException.class, () -> filmController.addLike(film1.getId(), -1L));

        // Добавляем первого пользователя
        ResponseEntity<User> responseUser = userController.create(user1);
        assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        user1 = responseUser.getBody();
        assertNotNull(user1);
        assertNotNull((user1.getId()));

        // Добавляем лайк первого пользователя первому фильму
        ResponseEntity<Void> responseVoid = filmController.addLike(film1.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Добавляем лайк первого пользователя неизвестному фильму
        assertThrows(NotFoundException.class, () -> filmController.addLike(film1.getId() * 100, user1.getId()));

        // Добавляем лайк первого пользователя null фильму
        assertThrows(ValidationException.class, () -> filmController.addLike(null, user1.getId()));

        // Добавляем лайк первого пользователя отрицательному фильму
        assertThrows(NotFoundException.class, () -> filmController.addLike(-1 * film1.getId(), user1.getId()));

        // Удаляем лайк первого пользователя первому фильму
        responseVoid = filmController.removeLike(film1.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Повторно удаляем лайк первого пользователя первому фильму
        responseVoid = filmController.removeLike(film1.getId(), user1.getId());
        assertEquals(HttpStatus.OK, responseVoid.getStatusCode());

        // Удаляем лайк первого пользователя неизвестному фильму
        assertThrows(NotFoundException.class, () -> filmController.removeLike(100 * film1.getId(), user1.getId()));

        // Удаляем лайк первого пользователя null фильму
        assertThrows(ValidationException.class, () -> filmController.removeLike(null, user1.getId()));

        // Удаляем лайк первого пользователя отрицательному фильму
        assertThrows(NotFoundException.class, () -> filmController.removeLike(-1 * film1.getId(), user1.getId()));

        // Удаляем лайк null пользователя null фильму
        assertThrows(ValidationException.class, () -> filmController.removeLike(null, null));

        // Удаляем лайк неизвестного пользователя неизвестному фильму
        assertThrows(NotFoundException.class,
                () -> filmController.removeLike(100 * film1.getId(), 100 * user1.getId()));
    }
}
