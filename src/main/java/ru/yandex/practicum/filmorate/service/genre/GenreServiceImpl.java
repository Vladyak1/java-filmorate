package ru.yandex.practicum.filmorate.service.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        log.info("Получен жанр по id: {}", id);
        return genreStorage.getGenreById(id);
    }
}