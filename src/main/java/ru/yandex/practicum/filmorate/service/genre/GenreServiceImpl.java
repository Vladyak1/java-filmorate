package ru.yandex.practicum.filmorate.service.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreStorage genreStorage;

    @Override
    public List<Genre> getAllGenres() {
        log.info("Получен список жанров");
        return genreStorage.getAllGenres();
    }

    @Override
    public Genre getGenre(int id) {
        Genre result;
        try {
            result = genreStorage.getGenreById(id).get();
        } catch (EmptyResultDataAccessException e) {
            log.warn("Не удалось вернуть жанр c id: {}.", id);
            throw new NotFoundException("Не удалось вернуть жанр.");
        }
        return result;
    }
}