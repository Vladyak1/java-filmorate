package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> getDirectors();

    Optional<Director> getDirectorById(long id);

    Director saveDirector(Director director);

    void deleteDirector(long id);

    void addDirectorsToFilm(long filmId, List<Director> directors);

    Director updateDirector(Director director);
}
