-- Создание последовательностей

CREATE SEQUENCE IF NOT EXISTS seq_relationship_statuses INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_relationship_types INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_users_relationships INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_films_genres INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_genres INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_users INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_relationships INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_ratings INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_films INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;


CREATE SEQUENCE IF NOT EXISTS seq_users_films INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

-- Создание таблиц

CREATE TABLE IF NOT EXISTS users (
  id integer DEFAULT nextval('seq_users') NOT NULL,
  email varchar(254) NOT NULL,
  login varchar(254) NOT NULL,
  full_name text NULL,
  birthday date NOT NULL,
  CONSTRAINT users_pk PRIMARY KEY (id),
  CONSTRAINT users_unique_email UNIQUE (email),
  CONSTRAINT users_unique_login UNIQUE (login)
);

COMMENT ON TABLE users IS 'Таблица пользователей';

COMMENT ON COLUMN users.id IS 'Идентификатор записи';

COMMENT ON COLUMN users.email IS 'Адрес электронной почты';

COMMENT ON COLUMN users.login IS 'Логин';

COMMENT ON COLUMN users.full_name IS 'Имя';


CREATE TABLE IF NOT EXISTS relationship_types(
  id integer DEFAULT nextval('seq_relationship_types') NOT NULL,
  full_name text NOT NULL,
  CONSTRAINT relationship_types_pk PRIMARY KEY (id),
  CONSTRAINT relationship_types_unique_full_name UNIQUE (full_name)
);

COMMENT ON TABLE relationship_types IS 'Таблица типов отношений';

COMMENT ON COLUMN relationship_types.id IS 'Идентификатор записи';

COMMENT ON COLUMN relationship_types.full_name IS 'Наименование';


CREATE TABLE IF NOT EXISTS relationship_statuses(
  id integer DEFAULT nextval('seq_relationship_statuses') NOT NULL,
  full_name text NOT NULL,
  CONSTRAINT relationship_statuses_pk PRIMARY KEY (id),
  CONSTRAINT relationship_statuses_unique_full_name UNIQUE (full_name)
);

COMMENT ON TABLE relationship_statuses IS 'Таблица статусов отношений';

COMMENT ON COLUMN relationship_statuses.id IS 'Идентификатор записи';

COMMENT ON COLUMN relationship_statuses.full_name IS 'Наименование';


CREATE TABLE IF NOT EXISTS relationships (
  id integer DEFAULT nextval('seq_relationships') NOT NULL,
  type_id integer NOT NULL,
  status_id integer NOT NULL,
  CONSTRAINT relationships_pk PRIMARY KEY (id),
  CONSTRAINT relationships_relationship_types_fk FOREIGN KEY (type_id) REFERENCES relationship_types(id),
  CONSTRAINT relationships_relationship_statuses_fk FOREIGN KEY (status_id) REFERENCES relationship_statuses(id)
);


CREATE TABLE IF NOT EXISTS users_relationships(
  id integer DEFAULT nextval('seq_users_relationships') NOT NULL,
  user_id integer NOT NULL,
  relationship_id integer NOT NULL,
  CONSTRAINT users_relationships_pk PRIMARY KEY (id),
  CONSTRAINT users_relationships_users_fk FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT users_relationships_relationships_fk FOREIGN KEY (relationship_id) REFERENCES relationships(id),
  CONSTRAINT users_relationships_unique_user_id_relationship_id UNIQUE (user_id, relationship_id)
);

COMMENT ON TABLE users_relationships IS 'Таблица связей пользователей и отношений';

COMMENT ON COLUMN users_relationships.id IS 'Идентификатор записи';

COMMENT ON COLUMN users_relationships.user_id IS 'Ссылка на идентификатор пользователя';

COMMENT ON COLUMN users_relationships.relationship_id IS 'Ссылка на идентификатор отношений';


CREATE TABLE IF NOT EXISTS ratings (
  id integer DEFAULT nextval('seq_ratings') NOT NULL,
  full_name text NOT NULL,
  CONSTRAINT ratings_pk PRIMARY KEY(id),
  CONSTRAINT ratings_unique_full_name UNIQUE (full_name)
);

COMMENT ON TABLE ratings IS 'Таблица рейтингов';

COMMENT ON COLUMN ratings.id IS 'Идентификатор записи';

COMMENT ON COLUMN ratings.full_name IS 'Наименование';


CREATE TABLE IF NOT EXISTS films (
  id integer DEFAULT nextval('seq_films') NOT NULL,
  full_name text NOT NULL,
  description varchar(200) NOT NULL,
  release_date DATE, duration integer,
  rating_id integer,
  CONSTRAINT films_pk PRIMARY KEY(id),
  CONSTRAINT films_ratings_fk FOREIGN KEY (rating_id) REFERENCES ratings(id)
);

COMMENT ON TABLE films IS 'Таблица фильмов';

COMMENT ON COLUMN films.id IS 'Идентификатор записи';

COMMENT ON COLUMN films.full_name IS 'Наименование';

COMMENT ON COLUMN films.description IS 'Описание';

COMMENT ON COLUMN films.release_date IS 'Дата релиза';

COMMENT ON COLUMN films.duration IS 'Длительность';

COMMENT ON COLUMN films.rating_id IS 'Ссылка на рейтинг';


CREATE TABLE IF NOT EXISTS users_films(
  id integer DEFAULT nextval('seq_users_films') NOT NULL,
  user_id integer NOT NULL,
  film_id integer NOT NULL,
  CONSTRAINT users_films_pk PRIMARY KEY (id),
  CONSTRAINT users_films_unique_user_id_film_id UNIQUE (user_id, film_id),
  CONSTRAINT users_films_users_fk FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT users_films_films_fk FOREIGN KEY (film_id) REFERENCES films(id)
);

COMMENT ON TABLE users_films IS 'Таблица связей пользователей и фильмов';

COMMENT ON COLUMN users_films.id IS 'Идентификатор';

COMMENT ON COLUMN users_films.user_id IS 'Ссылка на идентификатор пользователя';

COMMENT ON COLUMN users_films.film_id IS 'Ссылка на идентификатор фильма';


CREATE TABLE IF NOT EXISTS genres(
  id integer DEFAULT nextval('seq_genres') NOT NULL,
  full_name text NOT NULL,
  CONSTRAINT genres_pk PRIMARY KEY(id),
  CONSTRAINT genres_unique_full_name UNIQUE (full_name)
);

COMMENT ON TABLE genres IS 'Таблица жанров';

COMMENT ON COLUMN genres.id IS 'Идентификатор записи';

COMMENT ON COLUMN genres.full_name IS 'Наименование';


CREATE TABLE IF NOT EXISTS films_genres(
  id integer DEFAULT nextval('seq_films_genres') NOT NULL,
  film_id integer NOT NULL,
  genre_id integer NOT NULL,
  CONSTRAINT films_genres_pk PRIMARY KEY (id),
  CONSTRAINT films_genres_unique_films_id_genre_id UNIQUE (film_id, genre_id),
  CONSTRAINT films_genres_films_fk FOREIGN KEY (film_id) REFERENCES films(id),
  CONSTRAINT films_genres_genres_fk FOREIGN KEY (genre_id) REFERENCES genres(id)
);

COMMENT ON TABLE films_genres IS 'Таблица связей между фильмами и жанрами';

COMMENT ON COLUMN films_genres.id IS 'Идентификатор записи';

COMMENT ON COLUMN films_genres.film_id IS 'Ссылка на идентификатор фильма';

COMMENT ON COLUMN films_genres.genre_id IS 'Ссылка на идентификатор жанра';

-- Заполнение справочников
WITH prepared_data AS
  (SELECT 1 AS id,
          'G' AS full_name
   UNION SELECT 2 AS id,
                'PG' AS full_name
   UNION SELECT 3 AS id,
                'PG-13' AS full_name
   UNION SELECT 4 AS id,
                'R' AS full_name
   UNION SELECT 5 AS id,
                'NC-17' AS full_name)
MERGE INTO ratings AS r USING prepared_data AS pd ON r.id = pd.id WHEN matched THEN
UPDATE
SET full_name = pd.full_name WHEN NOT matched THEN
INSERT (id,
        full_name)
VALUES(pd.id, pd.full_name);

WITH prepared_data AS
  (SELECT 1 AS id,
          'Комедия' AS full_name
   UNION SELECT 2 AS id,
                'Драма' AS full_name
   UNION SELECT 3 AS id,
                'Мультфильм' AS full_name
   UNION SELECT 4 AS id,
                'Триллер' AS full_name
   UNION SELECT 5 AS id,
                'Документальный' AS full_name
   UNION SELECT 6 AS id,
                'Боевик' AS full_name)
MERGE INTO genres AS g USING prepared_data AS pd ON g.id = pd.id WHEN matched THEN
UPDATE
SET full_name = pd.full_name WHEN NOT matched THEN
INSERT (id,
        full_name)
VALUES(pd.id, pd.full_name);

WITH prepared_data AS
  (SELECT 1 AS id,
          'Запрошено' AS full_name
   UNION SELECT 2 AS id,
                'Одобрено' AS full_name
   UNION SELECT 3 AS id,
                'Отклонено' AS full_name)
MERGE INTO relationship_statuses AS rs USING prepared_data AS pd ON rs.id = pd.id
AND rs.full_name = pd.full_name WHEN matched THEN
UPDATE
SET full_name = pd.full_name WHEN NOT matched THEN
INSERT (id,
        full_name)
VALUES(pd.id, pd.full_name);

WITH prepared_data AS
  (SELECT 1 AS id,
          'Дружба' AS full_name
   UNION SELECT 2 AS id,
                'Вражда' AS full_name
   UNION SELECT 3 AS id,
                'Семья' AS full_name
   UNION SELECT 4 AS id,
                'Любовь' AS full_name)
MERGE INTO relationship_types AS rt USING prepared_data AS pd ON rt.id = pd.id WHEN matched THEN
UPDATE
SET full_name = pd.full_name WHEN NOT matched THEN
INSERT (id,
        full_name)
VALUES(pd.id, pd.full_name);