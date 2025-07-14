package ru.yandex.practicum.filmorate.dal.user;

import java.sql.Types;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
            VALUES(:userEMail, :userLogin, :userName, :userBirthday)
            """;
    private static final String UPDATE_USER_QUERY = """
            UPDATE USERS
               SET EMAIL = :userEMail,
                   LOGIN = :userLogin,
                   FULL_NAME = :userName,
                   BIRTHDAY = :userBirthday
             WHERE ID = :userId
            """;
    private static final String GET_FRIENDSHIP_ID_QUERY = """
            SELECT 1 AS ID
              FROM FRIENDS f
             WHERE f.USER_ID = :userId
               AND f.OTHER_ID = :friendId
            """;
    private static final String INSERT_FRIENDSHIP_QUERY = """
            INSERT INTO FRIENDS (USER_ID, OTHER_ID)
            VALUES (:userId, :friendId)
            """;
    private static final String DELETE_LINK_BETWEEN_USERS_QUERY = """
            DELETE FROM FRIENDS f
             WHERE f.USER_ID = :userId
               AND f.OTHER_ID = :friendId
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
             WHERE UPPER(u.EMAIL) = :userEMail
               AND u.ID != :userId
            """;
    private static final String GET_EMAIL_BEFORE_INSERT_QUERY = """
            SELECT u.ID
              FROM USERS u
             WHERE UPPER(u.EMAIL) = :userEMail
            """;
    private static final String GET_LOGIN_BEFORE_UPDATE_QUERY = """
            SELECT u.ID
              FROM USERS u
             WHERE UPPER(u.LOGIN) = :userLogin
               AND u.ID != :userId
            """;
    private static final String GET_LOGIN_BEFORE_INSERT_QUERY = """
            SELECT u.ID
              FROM USERS u
             WHERE UPPER(u.LOGIN) = :userLogin
            """;

    @Autowired
    public UserDbStorage(NamedParameterJdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        super(jdbcTemplate, userRowMapper);
    }

    @Override
    public Collection<User> findAll(Integer size, Integer from) {
        log.debug("Запрос всех пользователей на уровне хранилища");
        log.debug("Размер запрашиваемой коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        // Составляем набор параметров
        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("size", size, Types.BIGINT)
                .addValue("from", from, Types.BIGINT);

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
                .addValue("filmId", filmId, Types.BIGINT);

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
                .addValue("userId", userId, Types.BIGINT);

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
                .addValue("userId", userId, Types.BIGINT)
                .addValue("friendId", friendId, Types.BIGINT);

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
                .addValue("userId", userId, Types.BIGINT);

        Optional<User> result = findOne(GET_USER_BY_ID_QUERY, parameterSource);

        log.debug("Возврат результата поиска на уровень сервиса");
        return result;
    }

    @Override
    public User createUser(User user) {
        log.debug("Создание пользователя на уровне хранилища");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userEMail", user.getEmail(), Types.VARCHAR)
                .addValue("userLogin", user.getLogin(), Types.VARCHAR)
                .addValue("userName", user.getName(), Types.NVARCHAR)
                .addValue("userBirthday", user.getBirthday(), Types.DATE);

        long id = insert(INSERT_USER_QUERY, parameterSource);
        if (id == 0) {
            throw new RuntimeException("Не удалось добавить пользователя в БД");
        } else {
            log.debug("Сгенерировано значение {}", id);
        }

        user.setId(id);
        log.debug("Значение присвоено id присвоено пользователю");

        log.debug("Возврат результатов создания на уровень сервиса");
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        log.debug("Изменение фильма на уровне хранилища");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userEMail", newUser.getEmail(), Types.VARCHAR)
                .addValue("userLogin", newUser.getLogin(), Types.VARCHAR)
                .addValue("userName", newUser.getName(), Types.NVARCHAR)
                .addValue("userBirthday", newUser.getBirthday(), Types.DATE)
                .addValue("userId", newUser.getId(), Types.BIGINT);

        long updatedRows = update(UPDATE_USER_QUERY, parameterSource);
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

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT)
                .addValue("friendId", friendId, Types.BIGINT);

        // Если ранее дружбы не было
        if (!exists(GET_FRIENDSHIP_ID_QUERY, parameterSource)) {
            log.debug("Дружба будет добавлена в БД");

            // Создаём её
            boolean isInserted = insertWithOutReturnId(INSERT_FRIENDSHIP_QUERY, parameterSource);
            if (!isInserted) {
                throw new RuntimeException(
                        "Не удалось добавить пользователю с id " + userId + "  друга с id "
                                + friendId);
            } else {
                log.debug("Пользователю с id {} добавлен друг с id {}", userId,
                        friendId);
            }
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

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT)
                .addValue("friendId", friendId, Types.BIGINT);

        //Если дружба есть
        if (exists(GET_FRIENDSHIP_ID_QUERY, parameterSource)) {
            log.debug("Дружба будет удалена из БД");

            long deletedRows = deleteOne(DELETE_LINK_BETWEEN_USERS_QUERY, parameterSource);

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

        MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT);

        long deletedRows = deleteOne(DELETE_USER_QUERY, parameterSource);

        if (deletedRows == 0) {
            throw new RuntimeException("Не удалось удалить пользователя с id " + userId);
        }
        log.debug("Пользователь с id {} удален из хранилища", userId);

        log.debug("Возврат результатов удаления на уровень сервиса");
    }

    @Override
    public void clearUsers() {
        log.debug("Очистка хранилища пользователей");

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        long deletedRows = deleteMany(DELETE_ALL_FILMS, parameterSource);
        log.debug("На уровне хранилища очищено {} запись(ей)", deletedRows);

        log.debug("Возврат результатов очистки на уровень сервиса");
    }

    @Override
    public Boolean isMailAlreadyUsed(User user) {
        log.debug("Проверка на использование адреса {} другими пользователями", user.getEmail());

        if (user.getId() == null) {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("userEMail", user.getEmail().toUpperCase(), Types.VARCHAR);

            return exists(GET_EMAIL_BEFORE_INSERT_QUERY, parameterSource);
        } else {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("userEMail", user.getEmail().toUpperCase(), Types.VARCHAR)
                    .addValue("userId", user.getId(), Types.BIGINT);

            return exists(GET_EMAIL_BEFORE_UPDATE_QUERY, parameterSource);
        }
    }

    @Override
    public Boolean isLoginAlreadyUsed(User user) {
        log.debug("Проверка на использование логина {} другими пользователями", user.getLogin());

        if (user.getId() == null) {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("userLogin", user.getLogin().toUpperCase(), Types.VARCHAR);

            // Проверяем использование почты всеми пользователями
            return exists(GET_LOGIN_BEFORE_INSERT_QUERY, parameterSource);
        } else {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("userLogin", user.getLogin().toUpperCase(), Types.VARCHAR)
                    .addValue("userId", user.getId(), Types.BIGINT);

            // Проверяем использование почты другими пользователями
            return exists(GET_LOGIN_BEFORE_UPDATE_QUERY, parameterSource);
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