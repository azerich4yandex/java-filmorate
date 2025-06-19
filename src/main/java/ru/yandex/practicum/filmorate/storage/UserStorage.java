package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

/**
 * Интерфейс обработки сущностей {@link User} на уровне хранилища
 */
@Service
public interface UserStorage {

    /**
     * Метод возвращает коллекцию {@link User} из хранилища
     *
     * @param size максимальный размер возвращаемой коллекции
     * @param from номер стартового элемента
     * @return коллекция {@link User}
     */
    Collection<User> findAll(Integer size, Integer from);

    /**
     * Метод возвращает коллекцию {@link User}, которым понравился фильм
     *
     * @param filmId идентификатор фильма
     * @return коллекция {@link User}, которым понравился фильм
     */
    Collection<User> findByFilmId(Long filmId);

    /**
     * Метод возвращает коллекцию друзей пользователя
     *
     * @param userId идентификатор пользователя
     * @return коллекция друзей пользователя
     */
    Collection<User> findFriends(Long userId);

    /**
     * Метод возвращает коллекцию общих друзей пользователя
     *
     * @param userId идентификатор первого пользователя
     * @param friendId идентификатор второго пользователя
     * @return коллекция общих друзей пользователя
     */
    Collection<User> findCommonFriends(Long userId, Long friendId);

    /**
     * Метод возвращает экземпляр класса {@link User} из хранилища на основе переданного идентификатора
     *
     * @param userId идентификатор пользователя
     * @return найденный экземпляр класса {@link User}
     */
    Optional<User> findById(Long userId);

    /**
     * Метод создаёт в хранилище переданный экземпляр класса {@link User}
     *
     * @param user экземпляр класса {@link User}
     * @return экземпляр класса {@link User} с заполненными автоматически генерируемыми полями из хранилища
     */
    User createUser(User user);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link User}
     *
     * @param newUser экземпляр класса {@link User} для обновления
     * @return обновленный экземпляр класса {@link User} из хранилища
     */
    User updateUser(User newUser);

    /**
     * Метод добавляет пользователя в коллекцию его друзей
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    void addFriend(Long userId, Long friendId);

    /**
     * Метод удаляет друга из коллекции пользователя
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    void removeFriend(Long userId, Long friendId);

    /**
     * Метод удаляет экземпляр класса {@link User}, найденный в хранилище по id
     *
     * @param userId идентификатор пользователя
     */
    void deleteUser(Long userId);

    /**
     * Метод очищает хранилище пользователей
     */
    void clearUsers();

    /**
     * Метод проверяет использование почтового адреса другими пользователями
     *
     * @param user экземпляр класса {@link User}
     * @return результат проверки
     */
    Boolean isMailAlreadyUsed(User user);

    /**
     * Метод проверяет использование логина другими пользователями
     *
     * @param user экземпляр класса {@link User}
     * @return результат проверки
     */
    Boolean isLoginAlreadyUsed(User user);
}
