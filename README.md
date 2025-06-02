# Структура БД приложения

![scheme.png](docs/scheme.png)

* Схема БД с использованием [инструмента](https://dbdiagram.io/d/Jajava-filmorate-683dc11961dc3bf08d2ab823)
* Скрипт развертывания схемы в БД (PostgreSQL v.17) доступен по [ссылке](docs/scheme.sql)
* Подробное описание таблиц доступно по [ссылке](docs/scheme.sql)

# Примеры запросов к таблицам

## Пользователи

* Получить список 10 пользователей: `UserController.findAll()`
  ```sql
  SELECT u.id,
         u.email,
         u.login,
         u.birthday
    FROM users u
   LIMIT 10 /*Размер выборки*/
  OFFSET 0 /*Отступ*/;
  ```
  
* Получить пользователя по идентификатору : `UserController.findById(@PathVariable Long id)`
  ```sql
  SELECT u.id,
         u.email,
         u.login,
         u.birthday
    FROM users u
   WHERE u.id = 1 /* id */;
  ```

* Получить список друзей пользователя: `UserController.findFriends(@PathVariable Long id)`
  ```sql
  SELECT u2.id,
         u2.email,
         u2.login,
         u2.birthday
    FROM users_relationships ur
   INNER JOIN users u
      ON ur.user_id = ur.user_id
   INNER JOIN relationships r
      ON ur.relationship_id = r.id
   INNER JOIN relationship_statuses rs
      ON r.status_id = rs.id
   INNER JOIN relationship_types rt
      ON r.type_id = rt.id
   INNER JOIN users_relationships ur2
      ON ur2.relationship_id = r.id
   INNER JOIN users u2
      ON ur2.user_id = u2.id
   WHERE ur.user_id = 1 /* id */
     AND rt.full_name = 'Дружба'
     AND rs.full_name = 'Одобрено';
  ```

* Получить список общих друзей пользователей:
  `UserController.findCommonFriends(@PathVariable Long id, @PathVariable Long otherId)`
  ```sql
  WITH first_user_friends
     AS (SELECT u2.id,
                u2.email,
                u2.login,
                u2.birthday
           FROM users_relationships ur
          INNER JOIN users u
                  ON ur.user_id = ur.user_id
          INNER JOIN relationships r
                  ON ur.relationship_id = r.id
          INNER JOIN relationship_statuses rs
                  ON r.status_id = rs.id
          INNER JOIN relationship_types rt
                  ON r.type_id = rt.id
          INNER JOIN users_relationships ur2
                  ON ur2.relationship_id = r.id
          INNER JOIN users u2
                  ON ur2.user_id = u2.id
          WHERE ur.user_id = 1 /* id */
                AND rt.full_name = 'Дружба'
                AND rs.full_name = 'Одобрено'),
  second_user_friends
     AS (SELECT u2.id,
                u2.email,
                u2.login,
                u2.birthday
           FROM users_relationships ur
          INNER JOIN users u
                  ON ur.user_id = ur.user_id
          INNER JOIN relationships r
                  ON ur.relationship_id = r.id
          INNER JOIN relationship_statuses rs
                  ON r.status_id = rs.id
          INNER JOIN relationship_types rt
                  ON r.type_id = rt.id
          INNER JOIN users_relationships ur2
                  ON ur2.relationship_id = r.id
          INNER JOIN users u2
                  ON ur2.user_id = u2.id
          WHERE ur.user_id = 2 /* otherId */
                AND rt.full_name = 'Дружба'
                AND rs.full_name = 'Одобрено')
  SELECT fuf.id,
         fuf.email,
         fuf.login,
         fuf.birthday
    FROM first_user_friends fuf
  INTERSECT
  SELECT suf.id,
         suf.email,
         suf.login,
         suf.birthday
    FROM second_user_friends suf;
  ```

* Создание пользователя: `UserController.create(@RequestBody User user)`
  ```sql
  INSERT INTO users
            (
                        email,
                        login,
                        full_name,
                        birthday
            )
  VALUES
            (
                        'a@a.ru',
                        'a',
                        NULL,
                        TO_DATE('01.01.2000', 'dd.mm.yyyy')
            )
  RETURNING id;
  ```

* Обновление пользователя: `UserController.update(@RequestBody User newUser)`
  ```sql
  UPDATE users
     SET email = 'a@b.ru',
         login = 'aa',
         full_name = 'Aa',
         birthday = TO_DATE('02.01.2000', 'dd.mm.yyyy')
   WHERE id = 1 /* id */;
  ```

* Добавление дружбы: `UserController.addFriend(@PathVariable Long id, @PathVariable Long friendId)`

Примечание: Бизнес-логика по проверке наличия запросов от одного пользователя другому, а так же изменение статуса дружбы
будет реализована на уровне приложения. Так же на уровне приложения в ходе работы алгоритмов будет получен идентификатор
общих отношений с типом `Дружба` и созданы/изменены необходимые записи в таблицах `relationships` и
`users_relationships` для обоих пользователей

* Удаление дружбы: `UserController.removeFriend(@PathVariable Long id, @PathVariable Long friendId)`

Примечание: Бизнес-логика по проверке наличия дружбы между пользователями, а так же изменение статуса дружбы будет
реализовано на уровне приложения. Так же на уровне приложения в ходе работы алгоритмов будет получен идентификатор общих
отношений с типом `Дружба` и при его наличии будут удалены/изменены необходимые записи в таблицах `relationships` и
`users_relationships` для обоих пользователей

* Удаление пользователя по идентификатору: `UserController.deleteUser(@PathVariable Long id)`
  ```sql
  DELETE FROM users
   WHERE id = 1 /* id */;
  ```

* Очистка таблицы пользователей: `UserController.clearUsers()`
  ```sql
  DELETE FROM users;
  ```

## Фильмы

* Получить список 10 фильмов: `FilmController.findAll()`
  ```sql
  SELECT fi.id,
         fi.full_name,
         fi.description,
         fi.duration,
         fi.release_date,
         r.full_name AS rating_name
    FROM films fi
    LEFT JOIN ratings r
      ON fi.rating_id = r.id
   LIMIT 10
  OFFSET 0;
  ```

* Получить фильм по идентификатору: `FilmController.findById(@PathVariable Long id)`
  ```sql
  SELECT fi.id,
         fi.full_name,
         fi.description,
         fi.duration,
         fi.release_date,
         r.full_name AS rating_name
    FROM films fi
    LEFT JOIN ratings r
      ON fi.rating_id = r.id
   WHERE fi.id = 1 /* id */;
  ```

* Получить список самых популярных фильмов:
  `FilmController.findPopular(@RequestParam(required = false, defaultValue = "10") Integer count)`
  ```sql
  SELECT fi.id,
         fi.full_name,
         fi.description,
         fi.duration,
         fi.release_date,
         r.full_name  AS rating_name,
         COUNT(uf.id) AS total_likes
    FROM films fi
    LEFT JOIN ratings r
      ON fi.rating_id = r.id
   INNER JOIN users_films uf
      ON fi.id = uf.film_id
   GROUP BY fi.id,
            fi.full_name,
            fi.description,
            fi.duration,
            fi.release_date,
            r.full_name
   ORDER BY 7 DESC
   LIMIT 10 /* count */;
  ```

* Добавление фильма: `FilmController.create(@RequestBody Film film)`
  ```sql
  INSERT INTO films
            (
                        full_name,
                        description
            )
  VALUES
            (
                        'А',
                        'Остросюжетный фильм про приключения А'
            )
  RETURNING id;
  ```

* Обновление фильма: `FilmController.update(@RequestBody Film film)`
  ```sql
  UPDATE films
     SET full_name = 'Б',
         description = 'Не очень интересный фильм про Б'
   WHERE id = 1 /* id */;
  ```

* Добавление лайка фильму: `FilmController.addLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId)`

Примечание: Бизнес-логика по проверке наличия лайков пользователя фильму, а так же добавление лайка при его отсутствии
будет реализована на уровне приложения. Так же на уровне приложения в ходе работы будут созданы/проигнорированы
необходимые записи в таблице `users_films`

* Удаление лайка с фильма: `FilmController.removeLike(@PathVariable(name = "id") Long filmId, @PathVariable Long userId)`

Примечание: Бизнес-логика по проверке наличия лайков пользователя фильму, а так же удаление лайка при его наличии
будет реализована на уровне приложения. Так же на уровне приложения в ходе работы будут удалены
необходимые записи в таблице `users_films`

* Удаление фильма по идентификатору: `FilmController.deleteFilm(@PathVariable(name = "id") Long filmId)`
  ```sql
  DELETE FROM films
   WHERE id = 1 /* id */;
  ```

* Очистка таблицы пользователей: `FilmController.clearFilms()`
  ```sql
  DELETE FROM films;
  ```