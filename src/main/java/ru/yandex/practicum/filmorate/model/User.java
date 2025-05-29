package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Пользователь
 */
@Builder
@Data
@EqualsAndHashCode(of = "id")
public class User {

    /**
     * Коллекция друзей пользователя
     */
    @JsonIgnore
    private final Map<Long, User> friends = new HashMap<>();

    /**
     * Коллекция фильмов, которые понравились пользователю
     */
    @JsonIgnore
    private final Map<Long, Film> likes = new HashMap<>();

    /**
     * Идентификатор сущности
     */
    private Long id;

    /**
     * Электронная почта
     */
    private String email;

    /**
     * Логин
     */
    private String login;

    /**
     * Отображаемое имя
     */
    @Getter(AccessLevel.NONE)
    private String name;

    /**
     * Дата рождения пользователя
     */
    private LocalDate birthday;

    /**
     * Метод возвращает логин в качестве имени, если оно не указано. В противном случае возвращает имя
     *
     * @return имя пользователя
     */
    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        } else {
            return name;
        }
    }

    /**
     * Метод добавляет пользователя в коллекцию друзей
     *
     * @param friend экземпляр класса {@link User}
     */
    public void addFriend(User friend) {
        // Добавляем друга, если его нет
        if (!friends.containsKey(friend.getId())) {
            friends.put(friend.getId(), friend);
        }
    }

    /**
     * Метод удаляет пользователя из коллекции друзей
     *
     * @param friendId идентификатор пользователя
     */
    public void removeFriend(Long friendId) {
        friends.remove(friendId);
    }

    /**
     * Метод добавляет фильм в коллекцию понравившихся пользователю
     *
     * @param film экземпляр класса {@link Film}
     */
    public void addLike(Film film) {
        // Добавляем лайк, если его нет
        if (!likes.containsKey(film.getId())) {
            likes.put(film.getId(), film);
        }
    }

    /**
     * Метод удаляет фильм из коллекции фильмов, понравившихся пользователю
     *
     * @param filmId идентификатор фильма
     */
    public void removeLike(Long filmId) {
        likes.remove(filmId);
    }
}
