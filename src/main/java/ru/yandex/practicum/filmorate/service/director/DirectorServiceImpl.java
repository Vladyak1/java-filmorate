package ru.yandex.practicum.filmorate.service.director;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {
    private final DirectorStorage directorStorage;

    @Override
    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    @Override
    public Director getDirectorById(long id) {
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Director not found with id: " + id));
    }

    @Override
    public Director createDirector(Director director) {
        return directorStorage.saveDirector(director);
    }

    @Override
    public Director updateDirector(Director director) {
        directorStorage.getDirectorById(director.getId())
                .orElseThrow(() -> new NotFoundException("Director not found with id: " + director.getId()));
        return directorStorage.updateDirector(director);
    }

    @Override
    public void deleteDirector(long id) {
        directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Director not found with id: " + id));
        directorStorage.deleteDirector(id);
    }
}
