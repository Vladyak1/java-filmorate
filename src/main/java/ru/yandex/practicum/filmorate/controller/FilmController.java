package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Количество добавленных фильмов {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film newFilm) {
        validateFilm(newFilm);
        log.debug("Фильм на добавление {} прошёл валидацию", newFilm);
        newFilm.setId(getNextFilmId());
        log.debug("Новому фильму {} присвоен Id = {}", newFilm, newFilm.getId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм добавлен: {}", newFilm);
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        validateFilm(updatedFilm);
        log.debug("Фильм на обновление {} прошёл валидацию", updatedFilm);
        if (films.containsKey(updatedFilm.getId())) {
            log.debug("Id фильма {} на обновление найден", updatedFilm);
            Film oldFilm = films.get(updatedFilm.getId());
            oldFilm.setDescription(updatedFilm.getDescription());
            log.debug("Фильму {} обновлено описание", updatedFilm);
            oldFilm.setDuration(updatedFilm.getDuration());
            log.debug("Фильму {} обновлена длительность", updatedFilm);
            oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
            log.debug("Фильму {} обновлена дата выхода", updatedFilm);
            oldFilm.setName(updatedFilm.getName());
            log.debug("Фильму {} обновлено имя", updatedFilm);
            log.info("Фильм обновлен: {}", updatedFilm);
            return oldFilm;
        }
        log.error("При попытке обновления фильма не был найден его id: {}", updatedFilm.getId());
        throw new ConditionsNotMetException("Фильм с id = " + updatedFilm.getId() + " не найден");
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
            log.error("При попытке создания/обновления была указана дата выхода ранее допустимой 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
