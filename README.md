# Структура БД приложения

![scheme.png](docs/scheme.png)

* Схема БД с использованием [инструмента](https://dbdiagram.io/d/Jajava-filmorate-683dc11961dc3bf08d2ab823)
* Скрипт развертывания схемы в БД (`PostgreSQL` v.16.9) доступен по [ссылке](docs/pgsql_scheme.sql)
* Скрипт развёртывания схемы в БД (`H2`) доступен по [ссылке](docs/h2_scheme.sql)
* Подробное описание таблиц доступно по [ссылке](docs/scheme.md)

# Примеры запросов к таблицам (справедливо для `H2`)

## Пользователи

* Получить список 100 пользователей:
  `UserController.findAll(@RequestParam(defaultValue = "100") int size, @RequestParam(defaultValue = "0") int from)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY,
         0 as MARK
    FROM USERS u
   ORDER BY u.ID
   LIMIT :size
  OFFSET :from
  ```

* Получить пользователя по идентификатору : `UserController.findById(@PathVariable Long id)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY,
         0 as MARK
    FROM USERS u
   WHERE u.ID = :userId
  ```

* Получить список друзей пользователя: `UserController.findFriends(@PathVariable Long userId)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY,
         0 as MARK
    FROM FRIENDS f
   INNER JOIN USERS u ON f.OTHER_ID = u.ID
   WHERE f.USER_ID = :userId
  ```

* Получить список общих друзей пользователей:
  `UserController.findCommonFriends(@PathVariable Long userId, @PathVariable Long otherId)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY,
         0 as MARK
    FROM FRIENDS f
   INNER JOIN USERS u ON f.OTHER_ID = u.ID
   WHERE f.USER_ID = :userId
  INTERSECT
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY,
         0 as MARK
    FROM FRIENDS f
   INNER JOIN USERS u ON f.OTHER_ID = u.ID
   WHERE f.USER_ID = :friendId
  ```

* Создание пользователя: `UserController.create(@RequestBody User user)`
  ```sql
  INSERT INTO USERS (EMAIL, LOGIN, FULL_NAME, BIRTHDAY)
  VALUES(:userEMail, :userLogin, :userName, :userBirthday)
  ```

* Обновление пользователя: `UserController.update(@RequestBody User newUser)`
  ```sql
  UPDATE USERS
     SET EMAIL = :userEMail,
         LOGIN = :userLogin,
         FULL_NAME = :userName,
         BIRTHDAY = :userBirthday
   WHERE ID = :userId
  ```

* Добавление дружбы: `UserController.addFriend(@PathVariable Long userId, @PathVariable Long friendId)`
  ```sql
  INSERT INTO FRIENDS (USER_ID, OTHER_ID)
  VALUES (:userId, :friendId)
  ```

* Удаление дружбы: `UserController.removeFriend(@PathVariable Long userId, @PathVariable Long friendId)`
  ```sql
  DELETE FROM FRIENDS f
   WHERE f.USER_ID = :userId
     AND f.OTHER_ID = :friendId
  ```

* Удаление пользователя по идентификатору: `UserController.deleteUser(@PathVariable Long userId)`
  ```sql
  DELETE FROM USERS
   WHERE ID = :userId
  ```

* Очистка таблицы пользователей: `UserController.clearUsers()`
  ```sql
  DELETE FROM USERS
  ```

## Фильмы

* Получить список из 100 фильмов:
  `FilmController.findAll(@RequestParam(name = "size", defaultValue = "100") int size, @RequestParam(name = "from", defaultValue = "0") int from)`
  ```sql
  SELECT f.ID,
         f.FULL_NAME,
         f.DESCRIPTION,
         f.RELEASE_DATE,
         f.DURATION,
         f.RATING_ID,
         r.FULL_NAME as rating_name
    FROM FILMS f
    LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
   ORDER BY f.ID
   LIMIT :size
  OFFSET :from
  ```

* Получить фильм по идентификатору: `FilmController.findById(@PathVariable Long id)`
  ```sql
  SELECT f.ID,
         f.FULL_NAME,
         f.DESCRIPTION,
         f.RELEASE_DATE,
         f.DURATION,
         f.RATING_ID,
         r.FULL_NAME as rating_name
    FROM FILMS f
    LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
   WHERE f.ID = :filmId
  ```

* Получить список самых популярных фильмов:
  `FilmController.findPopular(@RequestParam(name = "count", required = false, defaultValue = "10") Integer count, @RequestParam(name = "genreId", required = false) Long genreId, @RequestParam(name = "year", required = false) Integer year)`
  ```sql
  SELECT f.ID,
         f.FULL_NAME,
         f.DESCRIPTION,
         f.RELEASE_DATE,
         f.DURATION,
         f.RATING_ID,
         r.FULL_NAME AS rating_name,
         COUNT(uf.USER_ID) AS likes,
         NVL(AVG(uf.MARK), 0) AS rate
    FROM FILMS f
    LEFT JOIN USERS_FILMS uf ON f.ID = uf.FILM_ID
    LEFT JOIN RATINGS r ON f.RATING_ID = r.ID
    LEFT JOIN FILMS_GENRES fg ON f.ID = fg.FILM_ID
   WHERE (fg.GENRE_ID = :genreId OR :genreId IS NULL)
     AND (YEAR(f.RELEASE_DATE) = :year OR :year IS NULL)
   GROUP BY f.ID,
            r.ID
   ORDER BY rate DESC, likes DESC, f.ID ASC
   LIMIT :count
  ```

* Добавление фильма: `FilmController.create(@RequestBody Film film)`
  ```sql
  INSERT INTO FILMS(FULL_NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
  VALUES (:filmName, :filmDescription, :filmReleaseDate, :filmDuration, :ratingId)
  ```

* Обновление фильма: `FilmController.update(@RequestBody Film film)`
  ```sql
  UPDATE FILMS
     SET FULL_NAME = :filmName,
         DESCRIPTION = :filmDescription,
         RELEASE_DATE = :filmReleaseDate,
         DURATION = :filmDuration,
         RATING_ID = :ratingId
   WHERE ID = :filmId
  ```

* Добавление лайка фильму: `FilmController.addLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId)`
  ```sql
  INSERT INTO USERS_FILMS (FILM_ID, USER_ID, MARK)
  VALUES (:filmId, :userId, :mark)
  ```

* Удаление лайка с фильма:
  `FilmController.removeLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId)`
  ```sql
  DELETE FROM USERS_FILMS
   WHERE FILM_ID = :filmId
     AND USER_ID = :userId
  ```

* Удаление фильма по идентификатору: `FilmController.deleteFilm(@PathVariable(name = "id") Long filmId)`
  ```sql
  DELETE FROM FILMS
   WHERE id = :filmId
  ```

* Очистка таблицы пользователей: `FilmController.clearFilms()`
  ```sql
  DELETE FROM FILMS
  ```