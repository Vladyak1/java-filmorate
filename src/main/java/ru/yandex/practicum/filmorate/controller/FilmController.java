package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable long id) {
        return filmService.getFilm(id);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.updFilm(film);
    }

    @GetMapping
    public ArrayList<Film> findAll() {
        return new ArrayList<>(filmService.getFilms());
    }

    @DeleteMapping
    public void deleteAll() {
        filmService.delAllFilms();
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable long id) {
        filmService.delFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void delLike(@PathVariable long id, @PathVariable long userId) {
        filmService.delLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(required = false, defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorFilmsSorted(@PathVariable long directorId, @RequestParam String sortBy) {
        return filmService.getDirectorFilmsSorted(directorId, sortBy);
    }
}
