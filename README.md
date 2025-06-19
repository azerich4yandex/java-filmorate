# Структура БД приложения

![scheme.png](docs/scheme.png)

* Схема БД с использованием [инструмента](https://dbdiagram.io/d/Jajava-filmorate-683dc11961dc3bf08d2ab823)
* Скрипт развертывания схемы в БД (`PostgreSQL` v.16.9) доступен по [ссылке](docs/pgsql_scheme.sql)
* Скрипт развёртывания схемы в БД (`H2`) доступен по [ссылке](docs/h2_scheme.sql)
* Подробное описание таблиц доступно по [ссылке](docs/scheme.md)

# Примеры запросов к таблицам (справедливо для `H2`)

## Пользователи

* Получить список 100 пользователей:
  `UserController.findAll(@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "0") int from)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY
    FROM USERS u
   ORDER BY u.ID
   LIMIT 100
  OFFSET 0
  ```

* Получить пользователя по идентификатору : `UserController.findById(@PathVariable Long id)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY
    FROM USERS u
   WHERE u.ID = 1
  ```

* Получить список друзей пользователя: `UserController.findFriends(@PathVariable Long userId)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY
    FROM FRIENDS f
   INNER JOIN USERS u ON f.OTHER_ID = u.ID
   WHERE f.USER_ID = 1
  ```

* Получить список общих друзей пользователей:
  `UserController.findCommonFriends(@PathVariable Long userId, @PathVariable Long otherId)`
  ```sql
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY
    FROM FRIENDS f
   INNER JOIN USERS u ON f.OTHER_ID = u.ID
   WHERE f.USER_ID = 1
  INTERSECT
  SELECT u.ID,
         u.EMAIL,
         u.LOGIN,
         u.FULL_NAME,
         u.BIRTHDAY
    FROM FRIENDS f
   INNER JOIN USERS u ON f.OTHER_ID = u.ID
   WHERE f.USER_ID = 2
  ```

* Создание пользователя: `UserController.create(@RequestBody User user)`
  ```sql
  INSERT INTO USERS (EMAIL, LOGIN, FULL_NAME, BIRTHDAY)
  VALUES ('a@a.ru', 'a', NULL, to_date('01.01.2000', 'dd.mm.yyyy'))
  ```

* Обновление пользователя: `UserController.update(@RequestBody User newUser)`
  ```sql
  UPDATE USERS
     SET EMAIL = 'a@b.ru',
         LOGIN = 'aa',
         FULL_NAME = 'Aa',
         BIRTHDAY = TO_DATE('02.01.2000', 'dd.mm.yyyy')
   WHERE ID = 1
  ```

* Добавление дружбы: `UserController.addFriend(@PathVariable Long userId, @PathVariable Long friendId)`
  ```sql
  INSERT INTO FRIENDS (USER_ID, OTHER_ID)
  VALUES (1, 2)
  ```

* Удаление дружбы: `UserController.removeFriend(@PathVariable Long userId, @PathVariable Long friendId)`
  ```sql
  DELETE FROM FRIENDS
   WHERE USER_ID = 1
     AND OTHER_ID = 2
  ```

* Удаление пользователя по идентификатору: `UserController.deleteUser(@PathVariable Long id)`
  ```sql
  DELETE FROM USERS
   WHERE ID = 1
  ```

* Очистка таблицы пользователей: `UserController.clearUsers()`
  ```sql
  DELETE FROM USERS
  ```

## Фильмы

* Получить список первых 100 фильмов:
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
   LIMIT 100
  OFFSET 0
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
   WHERE f.ID = 1
  ```

* Получить список самых популярных фильмов:
  `FilmController.findPopular(@RequestParam(required = false, defaultValue = "10") Integer count)`
  ```sql
  SELECT f.ID,
         f.FULL_NAME,
         f.DESCRIPTION,
         f.DURATION,
         f.RELEASE_DATE,
         f.RATING_ID,
         r.FULL_NAME AS rating_name,
         COUNT(uf.ID) AS cnt
    FROM FILMS f
    LEFT JOIN RATINGS r
      ON f.RATING_ID = r.ID
   INNER JOIN USERS_FILMS uf
      ON f.ID = uf.FILM_ID
   GROUP BY f.ID,
         f.FULL_NAME,
         f.DESCRIPTION,
         f.DURATION,
         f.RELEASE_DATE,
         f.RATING_ID,
         r.FULL_NAME
   ORDER BY 8 DESC
   LIMIT 10
  ```

* Добавление фильма: `FilmController.create(@RequestBody Film film)`
  ```sql
  INSERT INTO FILMS (FULL_NAME, DESCRIPTION)
  VALUES ('А', 'Остросюжетный фильм про приключения А')
  ```

* Обновление фильма: `FilmController.update(@RequestBody Film film)`
  ```sql
  UPDATE FILMS
     SET FULL_NAME = 'Б',
         DESCRIPTION = 'Не очень интересный фильм про Б'
   WHERE ID = 1
  ```

* Добавление лайка фильму: `FilmController.addLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId)`
  ```sql
  INSERT INTO USERS_FILMS (FILM_ID, USER_ID)
  VALUES (1, 1)
  ```

* Удаление лайка с фильма:
  `FilmController.removeLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId)`
  ```sql
  DELETE FROM USERS_FILMS
   WHERE FILM_ID = 1
     AND USER_ID = 1
  ```

* Удаление фильма по идентификатору: `FilmController.deleteFilm(@PathVariable(name = "id") Long filmId)`
  ```sql
  DELETE FROM FILMS
   WHERE id = 1
  ```

* Очистка таблицы пользователей: `FilmController.clearFilms()`
  ```sql
  DELETE FROM FILMS
  ```