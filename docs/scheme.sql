-- # Предварительная очистка схемы
-- ## Таблицы
drop table if exists users_relationships;
drop table if exists relationships;
drop table if exists relationship_statuses;
drop table if exists relationship_types;
drop table if exists users_films;
drop table if exists users;
drop table if exists films_genres;
drop table if exists films;
drop table if exists genres;
drop table if exists ratings;

-- ## Последовательности
drop sequence if exists seq_relationship_statuses;
drop sequence if exists seq_relationship_types;
drop sequence if exists seq_users_relationships;
drop sequence if exists seq_films_genres;
drop sequence if exists seq_genres;
drop sequence if exists seq_ratings;
drop sequence if exists seq_users_films;
drop sequence if exists seq_relationships;
drop sequence if exists seq_users;
drop sequence if exists seq_films;


-- # Создание структуры БД
-- ## Последовательности
create sequence seq_relationship_statuses
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_relationship_types
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_users_relationships
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_films_genres
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_genres
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_users
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_relationships
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_ratings
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_films
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

create sequence seq_users_films
	increment by 1
	minvalue 1
	start 1
	cache 1
	no cycle;

-- ## Таблицы
create table users (
	id integer default nextval('seq_users') not null,
	email varchar(254) not null,
	login varchar(254) not null,
	full_name text null,
	birthday date not null,
	constraint users_pk primary key (id),
	constraint users_unique_email unique (email),
	constraint users_unique_login unique (login)
);
comment on table users is 'Таблица пользователей';
comment on column users.id is 'Идентификатор записи';
comment on column users.email is 'Адрес электронной почты';
comment on column users.login is 'Логин';
comment on column users.full_name is 'Имя';

create table relationship_types(
	id integer default nextval('seq_relationship_types') not null,
	full_name text not null,
	constraint relationship_types_pk primary key (id),
	constraint relationship_types_unique_full_name unique (full_name)
);
comment on table relationship_types is 'Таблица типов отношений';
comment on column relationship_types.id is 'Идентификатор записи';
comment on column relationship_types.full_name is 'Наименование';

create table relationship_statuses(
	id integer default nextval('seq_relationship_statuses') not null,
	full_name text not null,
	constraint relationship_statuses_pk primary key (id),
	constraint relationship_statuses_unique_full_name unique (full_name)
);
comment on table relationship_statuses is 'Таблица статусов отношений';
comment on column relationship_statuses.id is 'Идентификатор записи';
comment on column relationship_statuses.full_name is 'Наименование';

create table relationships (
	id integer default nextval('seq_relationships') not null,
	type_id integer not null,
	status_id integer not null,
	constraint relationships_pk primary key (id),
	constraint relationships_relationship_types_fk foreign key (type_id) references relationship_types(id),
	constraint relationships_relationship_statuses_fk foreign key (status_id) references relationship_statuses(id)
);

create table users_relationships(
	id integer default nextval('seq_users_relationships') not null,
	user_id integer not null,
	relationship_id integer not null,
	constraint users_relationships_pk primary key (id),
	constraint users_relationships_users_fk foreign key (user_id) references users(id),
	constraint users_relationships_relationships_fk foreign key (relationship_id) references relationships(id),
	constraint users_relationships_unique_user_id_relationship_id unique (user_id, relationship_id)
);
comment on table users_relationships is 'Таблица связей пользователей и отношений';
comment on column users_relationships.id is 'Идентификатор записи';
comment on column users_relationships.user_id is 'Ссылка на идентификатор пользователя';
comment on column users_relationships.relationship_id is 'Ссылка на идентификатор отношений';

create table ratings (
	id integer default nextval('seq_ratings') not null,
	full_name text not null,
	constraint ratings_pk primary key(id),
	constraint ratings_unique_full_name unique (full_name)
);
comment on table ratings is 'Таблица рейтингов';
comment on column ratings.id is 'Идентификатор записи';
comment on column ratings.full_name is 'Наименование';

create table films (
	id integer default nextval('seq_films') not null,
	full_name text not null,
	description varchar(200) not null,
	release_date DATE,
	duration integer,
	rating_id integer,
	constraint films_pk primary key(id),
	constraint films_ratings_fk foreign key (rating_id) references ratings(id)
);
comment on table films is 'Таблица фильмов';
comment on column films.id is 'Идентификатор записи';
comment on column films.full_name is 'Наименование';
comment on column films.description is 'Описание';
comment on column films.release_date is 'Дата релиза';
comment on column films.duration is 'Длительность';
comment on column films.rating_id is 'Ссылка на рейтинг';

create table users_films(
	id integer default nextval('seq_users_films') not null,
	user_id integer not null,
	film_id integer not null,
	constraint users_films_pk primary key (id),
	constraint users_films_unique_user_id_film_id unique (user_id, film_id),
	constraint users_films_users_fk foreign key (user_id) references users(id),
	constraint users_films_films_fk foreign key (film_id) references films(id)
);
comment on table users_films is 'Таблица связей пользователей и фильмов';
comment on column users_films.id is 'Идентификатор';
comment on column users_films.user_id is 'Ссылка на идентификатор пользователя';
comment on column users_films.film_id is 'Ссылка на идентификатор фильма';

create table genres(
	id integer default nextval('seq_genres') not null,
	full_name text not null,
	constraint genres_pk primary key(id),
	constraint genres_unique_full_name unique (full_name)
);
comment on table genres is 'Таблица жанров';
comment on column genres.id is 'Идентификатор записи';
comment on column genres.full_name is 'Наименование';

create table films_genres(
	id integer default nextval('seq_films_genres') not null,
	film_id integer not null,
	genre_id integer not null,
	constraint films_genres_pk primary key (id),
	constraint films_genres_unique_films_id_genre_id unique (film_id, genre_id),
	constraint films_genres_films_fk foreign key (film_id) references films(id),
	constraint films_genres_genres_fk foreign key (genre_id) references genres(id)
);
comment on table films_genres is 'Таблица связей между фильмами и жанрами';
comment on column films_genres.id is 'Идентификатор записи';
comment on column films_genres.film_id is 'Ссылка на идентификатор фильма';
comment on column films_genres.genre_id is 'Ссылка на идентификатор жанра';