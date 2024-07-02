package ru.yandex.practicum.filmorate.service.reviews;

import ru.yandex.practicum.filmorate.model.Reviews;

public interface ReviewsService {

    Reviews createReview(Reviews reviews);

    Reviews updateReview(Reviews reviews);

    void deleteReview(Long id);

    Reviews getReviewById(Long id);
}
