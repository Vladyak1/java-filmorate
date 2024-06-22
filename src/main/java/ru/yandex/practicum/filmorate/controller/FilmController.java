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

    //добавление фильма
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    //получение фильма
    @GetMapping("/{id}")
    public Film findFilm(@PathVariable long id) {
        return filmService.getFilm(id);
    }

    //обновление фильма
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.updFilm(film);
    }

    //получение всех фильмов
    @GetMapping
    public ArrayList<Film> findAll() {
        return new ArrayList<>(filmService.getFilms());
    }

    //удаление всех фильмов
    @DeleteMapping
    public void deleteAll() {
        filmService.delAllFilms();
    }

    //удаление фильма по id
    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable long id) {
        filmService.delFilm(id);
    }

    //пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    //пользователь удаляет лайк
    @DeleteMapping("/{id}/like/{userId}")
    public void delLike(@PathVariable long id, @PathVariable long userId) {
        filmService.delLike(id, userId);
    }

    //возвращает список из первых count фильмов по количеству лайков
    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") long count) {
        return new ArrayList<>(filmService.getPopularFilms(count));
    }
}
