package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public List<Film> getAllFilms() {
        log.info("Количество добавленных фильмов {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addFilm(Film newFilm) {
        validateFilm(newFilm);
        newFilm.setId(getNextFilmId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм добавлен: {}", newFilm);
        return newFilm;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        validateFilm(updatedFilm);
        if (films.containsKey(updatedFilm.getId())) {
            Film oldFilm = films.get(updatedFilm.getId());
            oldFilm.setDescription(updatedFilm.getDescription());
            oldFilm.setDuration(updatedFilm.getDuration());
            oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
            oldFilm.setName(updatedFilm.getName());
            log.info("Фильм обновлен: {}", updatedFilm);
            return oldFilm;
        }
        log.error("При попытке обновления фильма не был найден его id: {}", updatedFilm.getId());
        throw new NotFoundException("Фильм с id = " + updatedFilm.getId() + " не найден");
    }

    @Override
    public Film getFilmById(Long id) {
        if (!films.containsKey(id)) {
            log.error("При попытке получения фильма не был найден его id: {}", id);
            throw new NotFoundException("Фильм по указанному ID не найден");
        }
        return films.get(id);
    }

    @Override
    public void checkIdForFilm(Long filmId) {
        Optional<Film> maybeFilm = Optional.ofNullable(getFilmById(filmId));
        if (maybeFilm.isEmpty()) {
            log.error("По Id {} не закреплено фильма", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
    }

    private long getNextFilmId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("При попытке создания/обновления была указана дата выхода ранее допустимой: {}",
                    MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
    }
}
