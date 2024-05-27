package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long id, Long userId) {
        filmStorage.checkIdForFilm(id);
        userStorage.checkIdForUser(userId);
        Film film = filmStorage.getFilmById(id);
        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }
        film.getLikes().add(userId);
        log.info("Лайк добавлен, теперь их {}", film.getLikes().size());
    }

    public void deleteLike(Long id, Long userId) {
        filmStorage.checkIdForFilm(id);
        userStorage.checkIdForUser(userId);
        filmStorage.getFilmById(id).getLikes().remove(userId);
        log.info("Лайк удалён, теперь их {}", filmStorage.getFilmById(id).getLikes().size());
    }

    public List<Film> getTheFilmsByRating(Long count) {
        log.info("Выведен список популярных фильмов в размере {} строк.", count);
        return filmStorage.getAllFilms().stream()
                .sorted((o, o1) -> o1.getLikes().size() - o.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
