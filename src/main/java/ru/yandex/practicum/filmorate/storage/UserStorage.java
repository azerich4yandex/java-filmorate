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
     * @return коллекция {@link User}
     */
    Collection<User> findAll();

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
    User create(User user);

    /**
     * Метод обновляет в хранилище переданный экземпляр класса {@link User}
     *
     * @param newUser экземпляр класса {@link User} для обновления
     * @return обновленный экземпляр класса {@link User} из хранилища
     */
    User update(User newUser);

    /**
     * Метод сохраняет в хранилище переданный экземпляр класса {@link User}
     *
     * @param user экземпляр класса {@link User}
     */
    void save(User user);

    /**
     * Метод удаляет из хранилища переданный экземпляр класса
     *
     * @param userId идентификатор пользователя
     */
    void delete(Long userId);

    /**
     * Метод очищает хранилище пользователей
     */
    void clear();

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
