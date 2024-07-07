drop table if exists users, films, mpa_ratings, friends, genres, film_genres, film_likes, reviews, reviews_likes cascade;
drop table if exists directors cascade;
drop table if exists film_director cascade;
drop table if exists events cascade;

create table if not exists users
(
    id       integer generated by default as identity not null PRIMARY KEY,
    email    varchar(150)                             NOT NULL UNIQUE,
    login    varchar(150)                             NOT NULL UNIQUE,
    name     varchar(150),
    birthday date
);

create table if not exists mpa_ratings
(
    id   integer PRIMARY KEY,
    name varchar(150) NOT NULL
);

create table if not exists films
(
    id            integer generated by default as identity not null PRIMARY KEY,
    name          varchar(150)                             NOT NULL,
    description   varchar(500),
    release_date  date,
    duration      integer,
    rating_mpa_id integer REFERENCES mpa_ratings (id) on delete cascade
);

create table if not exists friends
(
    user_id_1 integer REFERENCES users (id) on delete cascade,
    user_id_2 integer REFERENCES users (id) on delete cascade,
    PRIMARY KEY (user_id_1, user_id_2)
);

create table if not exists genres
(
    id   integer PRIMARY KEY,
    name varchar(150)
);

create table if not exists film_genres
(
    film_id  integer REFERENCES films (id) on delete cascade,
    genre_id integer REFERENCES genres (id) on delete cascade,
    PRIMARY KEY (film_id, genre_id)
);

create table if not exists film_likes
(
    user_id integer REFERENCES users (id) on delete cascade,
    film_id integer REFERENCES films (id) on delete cascade,
    PRIMARY KEY (user_id, film_id)
);

create table if not exists directors
(
    director_id   integer generated by default as identity primary key,
    director_name varchar(255)
);

create table if not exists film_director
(
    film_id     integer references films (id) on delete cascade,
    director_id integer references directors (director_id) on delete cascade,
    primary key (film_id)
);

create table if not exists reviews
(
    id         long generated by default as identity not null PRIMARY KEY,
    content    varchar(255),
    isPositive boolean,
    user_id    long REFERENCES users (id) on delete cascade,
    film_id    long REFERENCES films (id) on delete cascade,
    useful     long
);

create table if not exists reviews_likes
(
    reviews_id long REFERENCES reviews (id) on delete cascade,
    user_id    long REFERENCES users (id) on delete cascade,
    isLike     boolean,
    PRIMARY KEY (reviews_id, user_id)
);

create table if not exists events
(
    event_id   integer generated by default as identity primary key,
    user_id    integer references users (id) on DELETE cascade,
    entity_id  integer,
    event_type varchar(50),
    operation  varchar(50),
    timestamp  timestamp default current_timestamp
)