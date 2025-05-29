package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Фильм
 */
@Builder
@Data
@EqualsAndHashCode(of = "id")
public class Film {

    /**
     * Список пользователей, которым понравился фильм
     */
    private final Map<Long, User> likes = new HashMap<>();
    /**
     * Идентификатор сущности
     */
    private Long id;
    /**
     * Название
     */
    private String name;
    /**
     * Описание
     */
    private String description;
    /**
     * Дата релиза
     */
    private LocalDate releaseDate;
    /**
     * Длительность
     */
    private Integer duration;

    /**
     * Метод добавляет в коллекцию лайков пользователя, которому понравился фильм
     *
     * @param user экземпляр класса {@link User}
     */
    public void addUsersLike(User user) {
        if (!likes.containsKey(user.getId())) {
            likes.put(user.getId(), user);
        }
    }

    /**
     * Метод удаляет из коллекции лайков пользователя, которому фильм больше не нравится
     *
     * @param userId идентификатор пользователя
     */
    public void removeUsersLike(Long userId) {
        likes.remove(userId);
    }
}
