package ru.yandex.practicum.filmorate.storage.reviews;

import ru.yandex.practicum.filmorate.model.Reviews;

import java.util.List;

public interface ReviewsStorage {

    Reviews save(Reviews entity);

    Reviews update(Reviews entity);

    void delete(Long id);

    Reviews findById(Long id);

    List<Reviews> findAllWithLimit(Integer count);

    List<Reviews> findAllByFilmIdWithLimit(Long id, Integer count);
}
