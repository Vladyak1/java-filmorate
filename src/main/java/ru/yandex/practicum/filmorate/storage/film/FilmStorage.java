package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    void delFilm(Long id);

    void delAllFilms();

    Film updFilm(Film film);

    List<Film> getFilms();

    void addLike(long filmId, long userId);

    void delLike(long filmId, long userId);

    List<Film> getPopularFilms(long count);

    Film findFilm(Long id);

    void addFilmGenre(Long filmId, Integer genreId);

    List<Genre> getFilmGenres(Long filmId);

    void delFilmGenres(Long filmId);

    Mpa getFilmMpa(Long filmId);

    void delFilmMpa(Long filmId);

    List<Film> getDirectorFilmsSorted(long directorId, String sort);

    List<Film> getFilmListBySearch(String textForSearch, Boolean searchByDirector, Boolean searchByTitle);

    List<Director> getDirectorsByFilmId(Long filmId);
}