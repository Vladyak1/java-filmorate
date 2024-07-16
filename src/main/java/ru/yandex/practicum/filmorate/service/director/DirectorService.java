package ru.yandex.practicum.filmorate.service.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorService {
    List<Director> getDirectors();

    Director getDirectorById(long id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(long id);
}
