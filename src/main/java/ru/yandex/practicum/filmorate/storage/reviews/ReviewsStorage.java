package ru.yandex.practicum.filmorate.storage.reviews;

import ru.yandex.practicum.filmorate.model.Reviews;

public interface ReviewsStorage {

    Reviews save(Reviews entity);

    Reviews update(Reviews entity);

    void delete(Long id);

    Reviews findById(Long id);
}
