package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    Film addFilm(Film film);

    void delFilm(Long id);

    void delAllFilms();

    Film updFilm(Film film);

    List<Film> getFilms();

    void addLike(long filmId, long userId);

    void delLike(long filmId, long userId);

    List<Film> getPopularFilms(long count);

    Film getFilm(long id);
}
