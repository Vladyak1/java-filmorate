package ru.yandex.practicum.filmorate.service.reviews;

import ru.yandex.practicum.filmorate.model.Reviews;

import java.util.List;

public interface ReviewsService {

    Reviews createReview(Reviews reviews);

    Reviews updateReview(Reviews reviews);

    void deleteReview(Long id);

    Reviews getReviewById(Long id);

    List<Reviews> getReviewsByFilmId(Long id, Integer count);

    Reviews addLike(Long reviewsId, Long userId);

    Reviews addDislike(Long reviewsId, Long userId);

    Reviews removeLike(Long reviewsId, Long userId);

    Reviews removeDislike(Long reviewsId, Long userId);
}
