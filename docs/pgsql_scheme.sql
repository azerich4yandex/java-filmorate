-- Создание последовательностей
CREATE SEQUENCE IF NOT EXISTS seq_genres INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS seq_users INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS seq_ratings INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS seq_films INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS seq_reviews INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS seq_directors INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

CREATE SEQUENCE IF NOT EXISTS seq_feed INCREMENT BY 1 MINVALUE 1
START 1 CACHE 1 NO CYCLE;

-- Создание таблиц
CREATE TABLE IF NOT EXISTS users
(
  id integer DEFAULT nextval('seq_users') NOT NULL,
  email varchar(256) NOT NULL,
  login varchar(256) NOT NULL,
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

CREATE TABLE IF NOT EXISTS ratings
(
  id integer DEFAULT nextval('seq_ratings') NOT NULL,
  full_name text NOT NULL,
  CONSTRAINT ratings_pk PRIMARY KEY(id),
  CONSTRAINT ratings_unique_full_name UNIQUE (full_name)
);
COMMENT ON TABLE ratings IS 'Таблица рейтингов';
COMMENT ON COLUMN ratings.id IS 'Идентификатор записи';
COMMENT ON COLUMN ratings.full_name IS 'Наименование';

CREATE TABLE IF NOT EXISTS films
(
  id integer DEFAULT nextval('seq_films') NOT NULL,
  full_name text NOT NULL,
  description varchar(200) NOT NULL,
  release_date DATE,
  duration integer,
  rating_id integer,
  CONSTRAINT films_pk PRIMARY KEY(id),
  CONSTRAINT films_ratings_fk FOREIGN KEY (rating_id) REFERENCES ratings(id),
  CONSTRAINT films_duration_ck CHECK (duration > 0)
);
COMMENT ON TABLE films IS 'Таблица фильмов';
COMMENT ON COLUMN films.id IS 'Идентификатор записи';
COMMENT ON COLUMN films.full_name IS 'Наименование';
COMMENT ON COLUMN films.description IS 'Описание';
COMMENT ON COLUMN films.release_date IS 'Дата релиза';
COMMENT ON COLUMN films.duration IS 'Длительность';
COMMENT ON COLUMN films.rating_id IS 'Ссылка на рейтинг';

CREATE TABLE IF NOT EXISTS users_films
(
  user_id integer NOT NULL,
  film_id integer NOT NULL,
  mark real,
  CONSTRAINT users_films_pk PRIMARY KEY (user_id, film_id),
  CONSTRAINT users_films_users_fk FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT users_films_films_fk FOREIGN KEY (film_id) REFERENCES films(id)
);
COMMENT ON TABLE users_films IS 'Таблица связей пользователей и фильмов';
COMMENT ON COLUMN users_films.user_id IS 'Ссылка на идентификатор пользователя';
COMMENT ON COLUMN users_films.film_id IS 'Ссылка на идентификатор фильма';
COMMENT ON COLUMN users_films.mark IS 'Оценка пользователя';

CREATE TABLE IF NOT EXISTS friends
(
  user_id integer NOT NULL,
  other_id integer NOT NULL,
  CONSTRAINT friends_pk PRIMARY KEY (user_id, other_id),
  CONSTRAINT friends_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT friends_users_other_id_fk FOREIGN KEY (other_id) REFERENCES users(id)
);
COMMENT ON TABLE friends IS 'Таблица связей пользователей';
COMMENT ON COLUMN friends.user_id IS 'Идентификатор пользователя-заявителя';
COMMENT ON COLUMN friends.other_id IS 'Идентификатор пользователя-адресата';

CREATE TABLE IF NOT EXISTS genres
(
  id integer DEFAULT nextval('seq_genres') NOT NULL,
  full_name text NOT NULL,
  CONSTRAINT genres_pk PRIMARY KEY(id),
  CONSTRAINT genres_unique_full_name UNIQUE (full_name)
);
COMMENT ON TABLE genres IS 'Таблица жанров';
COMMENT ON COLUMN genres.id IS 'Идентификатор записи';
COMMENT ON COLUMN genres.full_name IS 'Наименование';

CREATE TABLE IF NOT EXISTS films_genres
(
  film_id integer NOT NULL,
  genre_id integer NOT NULL,
  CONSTRAINT films_genres_pk PRIMARY KEY (film_id, genre_id),
  CONSTRAINT films_genres_films_fk FOREIGN KEY (film_id) REFERENCES films(id),
  CONSTRAINT films_genres_genres_fk FOREIGN KEY (genre_id) REFERENCES genres(id)
);
COMMENT ON TABLE films_genres IS 'Таблица связей между фильмами и жанрами';
COMMENT ON COLUMN films_genres.film_id IS 'Ссылка на идентификатор фильма';
COMMENT ON COLUMN films_genres.genre_id IS 'Ссылка на идентификатор жанра';

CREATE TABLE IF NOT EXISTS reviews (
  id integer DEFAULT nextval('seq_reviews') NOT NULL,
  user_id integer NOT NULL,
  film_id integer NOT NULL,
  content text NOT NULL,
  is_positive boolean,
  CONSTRAINT reviews_pk PRIMARY KEY (id),
  CONSTRAINT users_reviews_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT films_reviews_film_id_fk FOREIGN KEY (film_id) REFERENCES films (id)
);
COMMENT ON TABLE reviews IS 'Отзывы';
COMMENT ON COLUMN reviews.id IS 'Идентификатор записи';
COMMENT ON COLUMN reviews.user_id IS 'Идентификатор пользователя-автора';
COMMENT ON COLUMN reviews.film_id IS 'Идентификатор фильма, на который оставляют отзыв';
COMMENT ON COLUMN reviews.content IS 'Описание отзыва';
COMMENT ON COLUMN reviews.is_positive IS 'Признак положительного отзыва';

CREATE TABLE IF NOT EXISTS users_reviews (
  review_id integer NOT NULL,
  user_id integer,
  useful integer NOT NULL,
  CONSTRAINT users_reviews_pk PRIMARY KEY (review_id, user_id),
  CONSTRAINT users_reviews_reviews_review_id_fk FOREIGN KEY (review_id) REFERENCES reviews (id),
  CONSTRAINT users_reviews_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT users_reviews_useful_chk CHECK (useful in (-1, 1))
);

CREATE TABLE IF NOT EXISTS directors (
  id integer DEFAULT nextval('seq_directors') NOT NULL,
  full_name text NOT NULL,
  CONSTRAINT directors_pk PRIMARY KEY (id)
);
COMMENT ON TABLE directors IS 'Режиссеры';
COMMENT ON COLUMN directors.id IS 'Идентификатор записи';
COMMENT ON COLUMN directors.full_name IS 'Имя режиссера';

CREATE TABLE IF NOT EXISTS films_directors (
  film_id integer NOT NULL,
  director_id integer NOT NULL,
  CONSTRAINT films_directors_pk PRIMARY KEY (film_id, director_id),
  CONSTRAINT films_directors_films_film_id_fk FOREIGN KEY (film_id) REFERENCES films (id),
  CONSTRAINT films_directors_directors_director_id_fk FOREIGN KEY (director_id) REFERENCES directors (id)
);
COMMENT ON TABLE films_directors IS 'Связь фильмов и режиссеров';
COMMENT ON COLUMN films_directors.film_id IS 'Идентификатор фильма';
COMMENT ON COLUMN films_directors.director_id IS 'Идентификатор режиссера';

CREATE TABLE IF NOT EXISTS feed (
  event_id integer DEFAULT nextval('seq_feed') NOT NULL,
  entity_id integer NOT NULL,
  user_id integer NOT NULL,
  time_field timestamp NOT NULL,
  event_type text NOT NULL,
  operation_type text NOT NULL,
  CONSTRAINT feed_pk PRIMARY KEY (event_id),
  CONSTRAINT feed_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id)
);
COMMENT ON TABLE feed IS 'Лента событий';
COMMENT ON COLUMN feed.event_id IS 'Идентификатор записи';
COMMENT ON COLUMN feed.entity_id IS 'Идентификатор обработанной сущности';
COMMENT ON COLUMN feed.user_id IS 'Идентификатор пользователя';
COMMENT ON COLUMN feed.time_field IS 'Метка времени';
COMMENT ON COLUMN feed.event_type IS 'Тип события';
COMMENT ON COLUMN feed.operation_type IS 'Тип операции';

-- Заполнение справочников
WITH prepared_data AS
  (SELECT 1 AS id,
          'G' AS full_name
   UNION
   SELECT 2 AS id,
          'PG' AS full_name
   UNION
   SELECT 3 AS id,
          'PG-13' AS full_name
   UNION
   SELECT 4 AS id,
          'R' AS full_name
   UNION
   SELECT 5 AS id,
          'NC-17' AS full_name)
MERGE INTO ratings AS r USING prepared_data AS pd ON r.id = pd.id
WHEN matched THEN UPDATE SET full_name = pd.full_name
WHEN NOT matched THEN INSERT (id, full_name) VALUES(pd.id, pd.full_name);

WITH prepared_data AS
  (SELECT 1 AS id,
          'Комедия' AS full_name
   UNION
   SELECT 2 AS id,
          'Драма' AS full_name
   UNION
   SELECT 3 AS id,
          'Мультфильм' AS full_name
   UNION
   SELECT 4 AS id,
          'Триллер' AS full_name
   UNION
   SELECT 5 AS id,
          'Документальный' AS full_name
   UNION
   SELECT 6 AS id,
          'Боевик' AS full_name)
MERGE INTO genres AS g USING prepared_data AS pd ON g.id = pd.id
WHEN matched THEN UPDATE SET full_name = pd.full_name
WHEN NOT matched THEN INSERT (id, full_name) VALUES(pd.id, pd.full_name);