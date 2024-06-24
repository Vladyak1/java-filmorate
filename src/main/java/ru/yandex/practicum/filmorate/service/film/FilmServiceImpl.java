package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmDbStorage;
    private final UserService userService;
    private static final String FILM_DOES_NOT_EXIST = "Такого фильма не существует";
    private static final String USER_DOES_NOT_EXIST = "Пользователь не найден";
    private static final String MPA_DOES_NOT_EXIST = "Рейтинг MPA не найден";

    @Override
    public Film addFilm(Film film) {
        log.info("Добавление фильма {}", film);
        if (filmDbStorage.getFilms().contains(film)) {
            log.warn("Такой фильм уже добавлен");
            throw new ValidationException("Такой фильм уже добавлен");
        }

        Film result;

        try {
            result = filmDbStorage.addFilm(film);
        } catch (DuplicateKeyException e) {
            log.warn("Дублирование жанра");
            throw new IllegalArgumentException("Дублирование жанра");
        } catch (DataIntegrityViolationException e) {
            log.warn(MPA_DOES_NOT_EXIST);
            throw new IllegalArgumentException(MPA_DOES_NOT_EXIST);
        }
        return result;
    }

    @Override
    public void delFilm(Long id) {
        log.info("Удаление фильма с id: {}", id);
        try {
            filmDbStorage.findFilm(id);
        } catch (EmptyResultDataAccessException e) {
            log.warn(FILM_DOES_NOT_EXIST);
            throw new NotFoundException(FILM_DOES_NOT_EXIST);
        }
        log.info("Фильм с id: {} удален", id);
        filmDbStorage.delFilm(id);
    }

    @Override
    public void delAllFilms() {
        log.info("Список фильмов очищен");
        filmDbStorage.delAllFilms();
    }

    @Override
    public Film updFilm(Film film) {
        log.info("Обновление фильма {}", film);
        try {
            filmDbStorage.findFilm(film.getId());
        } catch (EmptyResultDataAccessException e) {
            log.warn(FILM_DOES_NOT_EXIST);
            throw new NotFoundException(FILM_DOES_NOT_EXIST);
        }
        return filmDbStorage.updFilm(film);
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = filmDbStorage.getFilms();
        log.info("Текущее количествоо фильмов: {}", films.size());
        return films;
    }

    @Override
    public void addLike(long filmId, long userId) {
        try {
            filmDbStorage.findFilm(filmId);
        } catch (EmptyResultDataAccessException e) {
            log.warn(FILM_DOES_NOT_EXIST);
            throw new NotFoundException(FILM_DOES_NOT_EXIST);
        }
        try {
            userService.getUser(userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }
        log.info("Фильму с id: {} поставили лайк", filmId);
        filmDbStorage.addLike(filmId, userId);
    }

    @Override
    public void delLike(long filmId, long userId) {
        try {
            filmDbStorage.findFilm(filmId);
        } catch (EmptyResultDataAccessException e) {
            log.warn(FILM_DOES_NOT_EXIST);
            throw new NotFoundException(FILM_DOES_NOT_EXIST);
        }
        try {
            userService.getUser(userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }
        log.info("У фильма с id: {} убрали лайк", filmId);
        filmDbStorage.delLike(filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(long count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Запрошено отрицательное число");
        }
        log.info("Список {} популярных фильма(ов)", count);
        return filmDbStorage.getPopularFilms(count);
    }

    @Override
    public Film getFilm(long id) {
        log.info("Получен фильм с id: {}", id);
        return filmDbStorage.findFilm(id);
    }
}
