package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.feed.FeedStorage;
import ru.yandex.practicum.filmorate.dal.user.UserStorage;
import ru.yandex.practicum.filmorate.dto.feed.FeedDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserShortDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FeedMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

/**
 * Класс предварительной обработки и валидации сущностей {@link User} на уровне сервиса
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;
    private final FilmService filmService;

    /**
     * Метод возвращает коллекцию {@link UserDto}
     *
     * @param size максимальный размер коллекции
     * @param from номер стартового элемента
     * @return результирующая коллекция
     */
    public Collection<UserDto> findAll(Integer size, Integer from) {
        log.debug("Запрос всех пользователей на уровне сервиса");
        log.debug("Размер запрашиваемой коллекции: {}", size);
        log.debug("Номер стартового элемента: {}", from);

        Collection<User> searchResult = userStorage.findAll(size, from);
        log.debug("На уровень сервиса вернулась коллекция размером {}", searchResult.size());

        Collection<UserDto> result = searchResult.stream().map(UserMapper::mapToUserDto).toList();

        // Перебираем полученную коллекцию
        for (UserDto user : result) {
            // Заполняем коллекции
            completeDto(user);
        }
        log.debug("Найденная коллекция преобразована. Размер коллекции после преобразования: {}", result.size());

        log.debug("Возврат результатов поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод получает список из {@link UserDto} на основе размера коллекции {@link UserDto#getFriends()}
     *
     * @param userId идентификатор пользователя
     * @return результирующая коллекция
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если пользователь не найден
     */
    public Collection<UserDto> findFriends(Long userId) throws ValidationException, NotFoundException {
        log.debug("Запрос друзей пользователя на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }
        log.debug("Передан userId пользователя: {}", userId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));
        log.debug("Найден пользователь с  id  {} ", user.getId());

        Collection<User> searchResult = userStorage.findFriends(user.getId());
        log.debug("Получена коллекция друзей пользователя размером {}", searchResult.size());

        Collection<UserDto> result = searchResult.stream().map(UserMapper::mapToUserDto).toList();
        // Перебираем полученную коллекцию
        for (UserDto model : result) {
            // Заполняем коллекции
            completeDto(model);
        }
        log.debug("Найденная коллекция друзей преобразована. Размер преобразованной коллекции {}", result.size());

        log.debug("Возврат результатов на уровень контроллера");
        return result;
    }

    /**
     * Метод получает список из {@link UserDto} на основе пересечения двух коллекций {@link UserDto#getFriends()}
     *
     * @param userId идентификатор первого пользователя
     * @param friendId идентификатор второго пользователя
     * @return результирующая коллекция
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если один из пользователей не найден
     */
    public Collection<UserDto> findCommonFriends(Long userId, Long friendId) throws ValidationException,
                                                                                    NotFoundException {
        log.debug("Запрос общих друзей двух пользователей на уровне сервиса");
        log.debug("Передан id первого пользователя: {}", userId);
        log.debug("Передан id второго пользователя: {}", friendId);

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }

        if (friendId == null) {
            throw new ValidationException("Передан пустой friendId");
        }

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        // Получаем список друзей пользователя
        Collection<User> searchResult = userStorage.findCommonFriends(user.getId(), friend.getId());
        log.debug("Получен список общих друзей между пользователем с id {} и другом с id {} размером {}", userId,
                friendId, searchResult.size());

        Collection<UserDto> result = searchResult.stream().map(UserMapper::mapToUserDto).toList();
        // Перебираем полученную коллекцию
        for (UserDto model : result) {
            // Заполняем коллекции
            completeDto(model);
        }
        log.debug("Коллекция общих друзей преобразована. Размер коллекции после преобразования: {}", result.size());

        log.debug("Возврат результатов поиска общих друзей на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает коллекцию рекомендаций {@link FilmDto} для пользователя
     *
     * @param userId идентификатор пользователя
     * @return коллекция фильмов для просмотра
     */
    public Collection<FilmDto> findUserRecommendations(Long userId) {
        log.debug("Запрос рекомендаций на уровне сервиса");
        log.debug("Передан идентификатор пользователя: {}", userId);

        if (userId == null) {
            throw new ValidationException("Id пользователя должен быть указан");
        }

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        log.debug("Вызов сервиса фильмов для поиска рекомендаций");
        Collection<FilmDto> result = filmService.findUserRecommendations(user.getId());
        log.debug("На уровень сервиса вернулась коллекция рекомендованных фильмов размером {}", result.size());

        log.debug("Возврат результатов поиска рекомендаций на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает коллекцию {@link FeedDto} для пользователя
     *
     * @param userId идентификатор пользователя
     * @return коллекция событий
     */
    public Collection<FeedDto> findFeed(Long userId) {
        log.debug("Запрос списка событий на уровне сервиса");
        log.debug("Передан  id  пользователя: {}", userId);

        if (userId == null) {
            throw new ValidationException("Id пользователя должен быть указан");
        }

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Collection<Feed> searchResult = feedStorage.findByUserId(user.getId());
        log.debug("На уровень сервиса вернулась коллекция событий размером {}", searchResult.size());

        Collection<FeedDto> result = searchResult.stream().map(FeedMapper::mapToFeedDto).toList();
        log.debug("Коллекция событий преобразована. Размер преобразованной коллекции {}", result.size());

        log.debug("Возврат ленты событий на уровень контроллера");
        return result;
    }

    /**
     * Метод возвращает экземпляр класса {@link UserDto}, найденный по идентификатору
     *
     * @param userId идентификатор пользователя
     * @return экземпляр класса {@link UserDto}
     * @throws ValidationException если передан пустой userId
     * @throws NotFoundException если пользователь не найден
     */
    public UserDto findById(Long userId) throws ValidationException, NotFoundException {
        log.debug("Поиск пользователя по id на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }
        log.debug("Передан id пользователя: {}", userId);

        User searchResult = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));
        log.debug("Пользователь с id {} найден в хранилище", searchResult.getId());

        UserDto result = UserMapper.mapToUserDto(searchResult);
        // Заполняем коллекции
        completeDto(result);
        log.debug("Найденный пользователь с id {} преобразован", result.getId());

        log.debug("Возврат результата поиска на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет переданную модель и передает для сохранения на уровень хранилища, после чего сохранённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохранённый экземпляр класса {@link NewUserRequest}
     * @return сохраненный экземпляр класса {@link UserDto}
     * @throws ValidationException в случае ошибок валидации
     */
    public UserDto create(NewUserRequest request) throws ValidationException {
        log.debug("Создание пользователя на уровне сервиса");

        User user = UserMapper.mapToUser(request);
        log.debug("Переданная модель преобразована");

        log.debug("Подготовка переданной модели");
        prepare(user);
        log.debug("Подготовка переданной модели завершена");

        log.debug("Валидация переданной модели");
        validate(user);
        log.debug("Валидация модели завершена");

        user = userStorage.createUser(user);

        UserDto result = UserMapper.mapToUserDto(user);// Перебираем полученную коллекцию
        // Заполняем коллекции
        completeDto(result);
        log.debug("Сохраненная модель преобразована");

        log.debug("Возврат результата добавления на уровень контроллера");
        return result;
    }

    /**
     * Метод проверяет переданную модель и передает для обновления на уровень хранилища, после чего сохранённую модель
     * возвращает на уровень контроллера
     *
     * @param request несохраненный экземпляр класса {@link UpdateUserRequest}
     * @return сохраненный экземпляр класса {@link UserDto}
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если пользователь для обновления не найден
     */
    public UserDto update(UpdateUserRequest request) throws ValidationException, NotFoundException {
        log.debug("Обновление пользователя на уровне сервиса");

        if (request.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Передан пользователь с id: {}", request.getId());

        // Получаем пользователя из хранилища
        User existingUser = userStorage.findById(request.getId()).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + request.getId() + " не найден в хранилище"));
        log.debug("В хранилище найден пользователь по id {}", existingUser.getId());

        User updatedUser = UserMapper.updateUserFields(existingUser, request);

        log.debug("Подготовка измененной модели");
        prepare(updatedUser);
        log.debug("Подготовка измененной модели завершена");

        // Проверяем переданного пользователя
        log.debug("Валидация обновленной модели");
        validate(updatedUser);
        log.debug("Валидация обновленной модели завершена");

        // Сохраняем изменения
        updatedUser = userStorage.updateUser(updatedUser);

        UserDto result = UserMapper.mapToUserDto(updatedUser);
        // Заполняем коллекции
        completeDto(result);
        log.debug("Обновленная модель преобразована");

        log.debug("Возврат результата обновления на уровень контроллера");
        return result;
    }

    /**
     * Метод добавляет друга в коллекцию друзей пользователя и пользователя в коллекцию друзей друга
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если один из пользователей не найден
     * @throws RuntimeException при ошибке добавления друга
     */
    public void addFriend(Long userId, Long friendId) throws ValidationException, NotFoundException, RuntimeException {
        log.debug("Добавление друзей на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }

        if (friendId == null) {
            throw new ValidationException("Передан пустой friendId");
        }

        // Получаем основного пользователя
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Получаем добавляемого друга
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден в хранилище"));

        // Если все пользователи успешно получены
        if (user != null && friend != null) {
            // Добавляем пользователю друга в друзья
            log.debug("Добавляем друга с id {} в коллекцию пользователя с id {}", friendId, userId);
            userStorage.addFriend(userId, friendId);
        } else {
            throw new RuntimeException("Во время добавления в друзья произошла непредвиденная ошибка");
        }

        log.debug("Регистрируем событие FRIEND ADD");
        Feed feed = Feed.builder()
                .entityId(friendId)
                .userId(userId)
                .timestamp(Timestamp.from(Instant.now()))
                .eventType(EventTypes.FRIEND)
                .operationType(OperationTypes.ADD)
                .build();
        feedStorage.addFeed(feed);
        log.debug("Событие FRIEND ADD зарегистрировано");

        log.debug("Возвращаем результат добавления на уровень контроллера");
    }

    /**
     * Метод удаляет друга из друзей пользователя и пользователя из друзей друга
     *
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException если один из пользователей не найден
     */
    public void removeFriend(Long userId, Long friendId) throws ValidationException, NotFoundException {
        log.debug("Удаление друзей на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой userId");
        }

        if (friendId == null) {
            throw new ValidationException("Передан пустой friendId");
        }

        // Проверяем наличие пользователя
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));

        // Проверяем наличие друга
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден в хранилище"));

        // Если все пользователи успешно получены
        if (user != null && friend != null) {
            // Удаляем из друзей пользователя друга
            log.debug("Удаляем друга с id {} из друзей пользователя с id {}", friend.getId(), user.getId());
            userStorage.removeFriend(user.getId(), friend.getId());
        } else {
            throw new RuntimeException("Во время удаления из друзей произошла непредвиденная ошибка");
        }

        log.debug("Регистрируем событие FRIEND REMOVE");
        Feed feed = Feed.builder()
                .entityId(friendId)
                .userId(userId)
                .timestamp(Timestamp.from(Instant.now()))
                .eventType(EventTypes.FRIEND)
                .operationType(OperationTypes.REMOVE)
                .build();
        feedStorage.addFeed(feed);
        log.debug("Событие FRIEND REMOVE зарегистрировано");

        log.debug("Возвращаем результат удаления на уровень контроллера");
    }

    /**
     * Метод удаляет пользователя и все его связи из хранилища
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    public void deleteUser(Long userId) throws NotFoundException {
        log.debug("Удаление пользователя на уровне сервиса");

        if (userId == null) {
            throw new ValidationException("Передан пустой id");
        }

        // Получаем пользователя из хранилища
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден в хранилище"));
        log.debug("В хранилище найден пользователь с  id  {} ", user.getId());

        // Получаем друзей пользователя
        Collection<Long> friends = user.getFriends().stream().toList();

        // Проверяем наличие друзей
        if (!friends.isEmpty()) {
            // Если друзья есть
            log.debug("У пользователя есть друзья");
            for (Long friendId : friends) {
                // Удаляем дружбу с пользователем
                removeFriend(friendId, user.getId());
            }
        }
        // Удаляем пользователя
        userStorage.deleteUser(user.getId());

        log.debug("Возврат результата удаления на уровень контроллера");
    }

    /**
     * Метод очищает хранилище пользователей
     */
    public void clearUsers() {
        log.debug("Очистка списка пользователей на уровне сервиса");

        // Очищаем хранилище
        userStorage.clearUsers();

        log.debug("Возврат результата очистки на уровень контроллера");
    }

    /**
     * Валидация сущности {@link User} на правильное заполнение ключевых полей
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     * @throws NotFoundException в случае ненайденных идентификаторов из коллекций
     */
    private void validate(User user) throws ValidationException, NotFoundException {
        // Валидация электронной почты
        validateEmail(user);

        // Валидация логина
        validateLogin(user);

        // Валидация даты рождения
        validateBirthday(user);

        // Валидация друзей
        validateFriends(user);
    }

    /**
     * Валидация электронной почты сущности {@link User}
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateEmail(User user) throws ValidationException {
        log.debug("Запускаем валидацию электронной почты");
        // Почта не должна быть пустой
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Передан пустой почтовый адрес");
        }

        // Почта должна соответствовать формату e-mail
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(user.getEmail());

        if (!matcher.matches()) {
            throw new ValidationException("Передан неправильный почтовый адрес: " + user.getEmail());
        }

        // Почта не должна быть ранее использована
        boolean exists = userStorage.isMailAlreadyUsed(user);

        if (exists) {
            throw new ValidationException("Электронная почта " + user.getEmail() + " уже используется");
        }
        log.debug("Передано корректное значение email: {}", user.getEmail());
        log.debug("Валидация электронной успешно почты завершена");
    }

    /**
     * Валидация логина сущности {@link User}
     *
     * @param user экземпляр сущности {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateLogin(User user) throws ValidationException {
        log.debug("Запускаем валидацию логина");
        // Логин не должен быть пустой
        if (user.getLogin() == null) {
            throw new ValidationException("Передан пустой логин");
        }

        // Логин не должен содержать пробелы
        if (user.getLogin().contains(" ")) {
            log.debug("Передан логин ({}), который содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не должен содержать пробелы: " + user.getLogin());
        }

        // Логин не должен быть ранее использован
        boolean exists = userStorage.isLoginAlreadyUsed(user);

        if (exists) {
            log.warn("Логин {} уже используется другим пользователем", user.getLogin());
            throw new ValidationException("Логин " + user.getLogin() + " уже используется");
        }
        log.debug("Передано корректное значение login: {}", user.getLogin());
        log.debug("Валидация логина успешно завершена");
    }

    /**
     * Валидация даты рождения сущности {@link User}
     *
     * @param user экземпляр класса {@link User}
     * @throws ValidationException в случае ошибок валидации
     */
    private void validateBirthday(User user) throws ValidationException {
        log.debug("Запускаем валидацию даты рождения");
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения должна быть меньше текущей даты");
        }
        log.debug("Передано корректное значение birthday: {}", user.getBirthday().format(DATE_FORMATTER));
        log.debug("Валидация даты рождения успешно завершена");
    }

    /**
     * Валидация коллекции друзей сущности {@link User}
     *
     * @param user экземпляр сущности {@link User}
     * @throws NotFoundException если друг не найден по идентификатору
     */
    private void validateFriends(User user) throws NotFoundException {
        log.debug("Запускаем валидацию списка друзей");
        if (!user.getFriends().isEmpty()) {
            for (Long friendId : user.getFriends()) {
                UserDto friend = findById(friendId);
                if (friend == null) {
                    throw new NotFoundException("Пользователь с id " + friendId + " не найден");
                }
            }
            log.debug("Передано корректное значение списка друзей: {}", user.getFriends());
        }
        log.debug("Валидация списка друзей успешно завершена");
    }

    /**
     * Метод заполняет данными коллекции DTO
     *
     * @param dto экземпляр класса {@link UserDto}
     */
    private void completeDto(UserDto dto) {
        if (dto != null) {
            log.debug("Формирование полей для пользователя с id {}", dto.getId());

            // Заполняем коллекцию друзей пользователя
            completeFriends(dto);
        }
    }

    /**
     * Метод заполняет данными коллекцию друзей DTO
     *
     * @param dto экземпляр класса {@link UserDto}
     */
    private void completeFriends(UserDto dto) {
        log.debug("Заполнение коллекции друзей пользователя");

        // Получаем список друзей
        Set<UserShortDto> friends = userStorage.findFriends(dto.getId()).stream()
                .map(UserMapper::mapToUserShortDto)
                .collect(Collectors.toSet());
        log.debug("Для пользователя с id {} получена коллекция друзей размером {}", dto.getId(), friends.size());

        // Устанавливаем полученную коллекцию пользователю
        dto.setFriends(friends);
        log.debug("Полученная коллекция друзей установлена пользователю");
    }

    /**
     * Метод очищает поля пользователя от мусорных символов
     *
     * @param user экземпляр сущности {@link User}
     */
    private void prepare(User user) {
        // Подготовка email
        user.setEmail(prepareStringValue(user.getEmail()));

        // Подготовка логина
        user.setLogin(prepareStringValue(user.getLogin()));

        // Подготовка имени
        user.setName(prepareStringValue(user.getName()));
    }

    /**
     * Метод очищает переданную строку от мусорных символов
     *
     * @param text строка для очистки
     * @return строка после очистки
     */
    private String prepareStringValue(String text) {
        // Удаляем переносы
        while (text.contains("\\n")) {
            text = text.replace("\\n", "\\s");
        }

        // Удаляем двойные пробелы
        while (text.contains("\\s\\s")) {
            text = text.replace("\\s\\s", "\\s");
        }

        // Возвращаем результат
        return text.trim().trim();
    }
}
