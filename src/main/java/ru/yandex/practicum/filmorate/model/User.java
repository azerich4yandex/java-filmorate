package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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
    private final Set<Long> friends = new HashSet<>();

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
     * Метод возвращает коллекцию идентификаторов друзей пользователя
     *
     * @return коллекция друзей идентификаторов пользователя
     */
    public Collection<Long> getFriends() {
        return this.friends.stream().toList();
    }

    /**
     * Метод обновляет коллекцию идентификаторов друзей пользователя
     *
     * @param friendIds коллекция идентификаторов друзей пользователя
     */
    public void setFriends(Collection<Long> friendIds) {
        this.friends.clear();
        this.friends.addAll(friendIds);
    }
}
