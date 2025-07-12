package ru.yandex.practicum.filmorate.dal.user;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseDbStorage;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Component
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String GET_ALL_USERS_QUERY = """
            SELECT u.ID,
                   u.EMAIL,
                   u.LOGIN,
                   u.FULL_NAME,
                   u.BIRTHDAY
              FROM USERS u
             ORDER BY u.ID
             LIMIT :size
            OFFSET :from
            """;
    private static final String GET_ALL_USER_FRIENDS_QUERY = """
            SELECT u.ID,
                   u.EMAIL,
                   u.LOGIN,
                   u.FULL_NAME,
                   u.BIRTHDAY
              FROM FRIENDS f
             INNER JOIN USERS u ON f.OTHER_ID = u.ID
             WHERE f.USER_ID = :userId
            """;
    private static final String GET_ALL_COMMON_FRIENDS_QUERY = """
            SELECT u.ID,
                   u.EMAIL,
                   u.LOGIN,
                   u.FULL_NAME,
                   u.BIRTHDAY
              FROM FRIENDS f
             INNER JOIN USERS u ON f.OTHER_ID = u.ID
             WHERE f.USER_ID = :userId
            INTERSECT
            SELECT u.ID,
                   u.EMAIL,
                   u.LOGIN,
                   u.FULL_NAME,
                   u.BIRTHDAY
              FROM FRIENDS f
             INNER JOIN USERS u ON f.OTHER_ID = u.ID
             WHERE f.USER_ID = :friendId
            """;
    private static final String GET_ALL_USERS_BY_FILM_ID_QUERY = """
            SELECT u.ID,
                   u.EMAIL,
                   u.LOGIN,
                   u.FULL_NAME,
                   u.BIRTHDAY
              FROM USERS_FILMS uf
             INNER JOIN USERS u ON uf.USER_ID = u.ID
             WHERE uf.FILM_ID = :filmId
             ORDER BY u.ID
            """;
    private static final String GET_USER_BY_ID_QUERY = """
            SELECT u.ID,
                   u.EMAIL,
                   u.LOGIN,
                   u.FULL_NAME,
                   u.BIRTHDAY
              FROM USERS u
             WHERE u.ID = :userId
            """;
    private static final String INSERT_USER_QUERY = """
            INSERT INTO USERS (EMAIL, LOGIN, FULL_NAME, BIRTHDAY)
            VALUES(?, ?, ?, ?)
            """;
    private static final String UPDATE_USER_QUERY = """
            UPDATE USERS
               SET EMAIL = ?,
                   LOGIN = ?,
                   FULL_NAME = ?,
                   BIRTHDAY = ?
             WHERE ID = ?
            """;
    private static final String GET_FRIENDSHIP_ID_QUERY = """
            SELECT 1 AS ID
              FROM FRIENDS f
             WHERE f.USER_ID = ?
               AND f.OTHER_ID = ?
            """;
    private static final String INSERT_FRIENDSHIP_QUERY = """
            INSERT INTO FRIENDS (USER_ID, OTHER_ID)
            VALUES (?, ?)
            """;
    private static final String DELETE_LINK_BETWEEN_USERS_QUERY = """
            DELETE FROM FRIENDS
             WHERE USER_ID = ?
               AND OTHER_ID = ?
            """;
    private static final String DELETE_USER_QUERY = """
            DELETE FROM USERS
             WHERE ID = ?
            """;
    private static final String DELETE_ALL_FILMS = """
            DELETE FROM USERS
            """;
    private static final String GET_EMAIL_BEFORE_UPDATE_QUERY = """
            SELECT u.ID
              FROM USERS u
             WHERE UPPER(u.EMAIL) = ?
               AND u.ID != ?
            """;
    private static final String GET_EMAIL_BEFORE_INSERT_QUERY = """
            SELECT u.ID
              FROM USERS u
             WHERE UPPER(u.EMAIL) = ?
            """;
    private static final String GET_LOGIN_BEFORE_UPDATE_QUERY = """
            SELECT u.ID
              FROM USERS u
             WHERE UPPER(u.LOGIN) = ?
               AND u.ID != ?
            """;
    private static final String GET_LOGIN_BEFORE_INSERT_QUERY = """
            SELECT u.ID
              FROM USERS u
             WHERE UPPER(u.LOGIN) = ?
            """;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper,
                         NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(jdbcTemplate, namedParameterJdbcTemplate, userRowMapper);
    }

    @Override
    public Collection<User> findAll(Integer size, Integer from) {
        log.debug("Запрос всех пользователей на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("size", size)
                .addValue("from", from);

        // Получаем коллекцию всех пользователей
        Collection<User> result = findMany(GET_ALL_USERS_QUERY, parameterSource);
        log.debug("Получена коллекция всех пользователей размером {}", result.size());

        // Возвращаем результат
        log.debug("Возврат результатов поиска на уровень сервиса");
        return result;
    }

    @Override
    public Collection<User> findByFilmId(Long filmId) {
        log.debug("Запрос лайков на уровне хранилища");

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId);

        Collection<User> result = findMany(GET_ALL_USERS_BY_FILM_ID_QUERY, parameterSource);
        log.debug("Получена коллекция пользователей размером {}", result.size());

        log.debug("Возврат лайков на уровень сервиса");
        return result;
    }

    @Override
    public Collection<User> findFriends(Long userId) {
        log.debug("Запрос друзей на уровне хранилища");
        log.debug("Идентификатор пользователя: {}", userId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId);

        Collection<User> result = findMany(GET_ALL_USER_FRIENDS_QUERY, parameterSource);
        log.debug("Получена коллекция идентификатором пользователей размером {}", result.size());

        log.debug("Возврат результатов поиска друзей на уровень сервиса");
        return result;

    }

    @Override
    public Collection<User> findCommonFriends(Long userId, Long friendId) {
        log.debug("Запрос списка общих друзей на уровне хранилища");
        log.debug("Идентификатор первого пользователя: {}", userId);
        log.debug("Идентификатор второго пользователя: {}", friendId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        Collection<User> result = findMany(GET_ALL_COMMON_FRIENDS_QUERY, parameterSource);
        log.debug("Получена коллекция размером {}", result.size());

        log.debug("Возврат результатов поиска общих друзей на уровень сервиса");
        return result;
    }

    @Override
    public Optional<User> findById(Long userId) {
        log.debug("Поиск пользователя по id на уровне хранилища");
        log.debug("Идентификатор искомого пользователя: {}", userId);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId);

        Optional<User> result = findOne(GET_USER_BY_ID_QUERY, parameterSource);

        log.debug("Возврат результата поиска на уровень сервиса");
        return result;
    }

    @Override
    public User createUser(User user) {
        log.debug("Создание пользователя на уровне хранилища");

        long id = insert(INSERT_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                Date.valueOf(user.getBirthday()));
        if (id == 0) {
            throw new RuntimeException("Не удалось добавить пользователя в БД");
        }
        log.debug("Сгенерировано значение {}", id);

        user.setId(id);
        log.debug("Значение присвоено id присвоено пользователю");

        log.debug("Возврат результатов создания на уровень сервиса");
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        log.debug("Изменение фильма на уровне хранилища");

        long updatedRows = update(UPDATE_USER_QUERY, newUser.getEmail(), newUser.getLogin(), newUser.getName(),
                newUser.getBirthday(), newUser.getId());
        log.debug("На уровне хранилища обновлено {} запись(ей)", updatedRows);

        propagateModel(newUser);

        log.debug("Возврат результатов изменения на уровень сервиса");
        return newUser;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        log.debug("Добавление друга на уровне хранилища");
        log.debug("Идентификатор основного пользователя: {}", userId);
        log.debug("Идентификатор друга: {}", friendId);

        // Если ранее дружбы не было
        if (!exists(GET_FRIENDSHIP_ID_QUERY, userId, friendId)) {
            log.debug("Дружба будет добавлена в БД");
            // Создаём её
            boolean isInserted = insertWithOutReturnId(INSERT_FRIENDSHIP_QUERY, userId, friendId);
            if (!isInserted) {
                throw new RuntimeException(
                        "Не удалось добавить дружбу между пользователем с id " + userId + "  и пользователем с id "
                                + friendId);
            }
            log.debug("Добавлена новая дружба между пользователем с id {} и пользователем с id {}", userId,
                    friendId);
        } else {
            log.debug("Дружба не будет добавлена в БД, т.к. уже существует");
        }

        log.debug("Возврат результата добавления дружбы на уровень сервиса");
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление друга на уровне хранилища");
        log.debug("Идентификатор  основного пользователя: {}", userId);
        log.debug("Идентификатор  друга: {}", friendId);

        //Если дружба есть
        if (exists(GET_FRIENDSHIP_ID_QUERY, userId, friendId)) {
            log.debug("Дружба будет удалена из БД");

            long deletedRows = deleteOne(DELETE_LINK_BETWEEN_USERS_QUERY, userId, friendId);

            if (deletedRows == 0) {
                throw new RuntimeException(
                        "Не удалось удалить дружбу между пользователем с id " + userId + " и пользователем с id "
                                + friendId);
            }
            log.debug("Дружба между пользователем с id {} и пользователем с id {} прекращена", userId, friendId);
        } else {
            log.debug("Дружба не будет удалена, т.к. не найдена в БД");
        }

        log.debug("Возврат результатов удаления дружбы на уровень сервиса");
    }

    @Override
    public void deleteUser(Long userId) {
        log.debug("Удаление пользователя на уровне хранилища");
        log.debug("Передан id пользователя: {}", userId);

        long deletedRows = deleteOne(DELETE_USER_QUERY, userId);

        if (deletedRows == 0) {
            throw new RuntimeException("Не удалось удалить пользователя с id " + userId);
        }
        log.debug("Пользователь с id {} удален из хранилища", userId);

        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearUsers() {
        log.debug("Очистка хранилища пользователей");

        long deletedRows = deleteMany(DELETE_ALL_FILMS);
        log.debug("На уровне хранилища очищено {} запись(ей)", deletedRows);

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    @Override
    public Boolean isMailAlreadyUsed(User user) {
        log.debug("Проверка на использование адреса {} другими пользователями", user.getEmail());

        if (user.getId() == null) {
            return exists(GET_EMAIL_BEFORE_INSERT_QUERY, user.getEmail().toUpperCase());
        } else {
            return exists(GET_EMAIL_BEFORE_UPDATE_QUERY, user.getEmail().toUpperCase(), user.getId());
        }
    }

    @Override
    public Boolean isLoginAlreadyUsed(User user) {
        log.debug("Проверка на использование логина {} другими пользователями", user.getLogin());

        if (user.getId() == null) {
            // Проверяем использование почты всеми пользователями
            return exists(GET_LOGIN_BEFORE_INSERT_QUERY, user.getLogin().toUpperCase());
        } else {
            // Проверяем использование почты другими пользователями
            return exists(GET_LOGIN_BEFORE_UPDATE_QUERY, user.getLogin().toUpperCase(), user.getId());
        }
    }

    /**
     * Метод распространяет коллекции переданного пользователя по структурам БД
     *
     * @param user экземпляр класса {@link User}
     */
    private void propagateModel(User user) {
        log.debug("Распространение коллекций пользователя");

        // Распространяем коллекцию друзей
        if (!user.getFriends().isEmpty()) {
            log.debug("Найдена коллекция друзей размером {}", user.getFriends().size());
            clearFriends(user);

            log.debug("Добавление нового списка друзей");
            for (Long friendId : user.getFriends()) {
                addFriend(user.getId(), friendId);
            }
            log.debug("Добавление списка завершено");
        } else {
            log.debug("Передана пустая коллекция друзей. Будет произведена очистка связей на уровне БД");
            clearFriends(user);
        }
    }

    /**
     * Метод очищает коллекции друзей пользователя в БД
     *
     * @param user экземпляр класса {@link User}
     */
    private void clearFriends(User user) {
        log.debug("Очистка имеющихся друзей");

        Collection<Long> friendIds = findFriends(user.getId()).stream().map(User::getId).toList();

        if (!friendIds.isEmpty()) {
            log.debug("В БД найдены связи, подлежащие удалению");
            for (Long friendId : friendIds) {
                removeFriend(user.getId(), friendId);
            }
        }
        log.debug("Очистка завершена");
    }
}
