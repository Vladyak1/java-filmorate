package ru.yandex.practicum.filmorate.service.reviews;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewsStorage;
import ru.yandex.practicum.filmorate.storage.reviews.like.ReviewsLikeDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsServiceImpl implements ReviewsService {
    private final ReviewsStorage reviewsStorage;
    private final ReviewsLikeDbStorage reviewsLikeStorage;
    private final static long INCREMENT_ON_LIKE = 1;
    private final static long DECREMENT_ON_DISLIKE = 1;

    @Override
    public Reviews createReview(Reviews reviews) {
        if (reviews.getFilmId() < 0) {
            throw new NotFoundException("Поле: filmId не может быть отрицательным");
        } else if (reviews.getUserId() < 0) {
            throw new NotFoundException("Поле: userId не может быть отрицательным");
        }
        return reviewsStorage.save(reviews);
    }

    @Override
    public Reviews updateReview(Reviews reviews) {
        return reviewsStorage.update(reviews);
    }

    @Override
    public void deleteReview(Long id) {
        reviewsStorage.delete(id);
    }

    @Override
    public Reviews getReviewById(Long id) {
        return reviewsStorage.findById(id);
    }

    @Override
    public List<Reviews> getReviewsByFilmId(Long id, Integer count) {
        if (id == null) {
            return reviewsStorage.findAllWithLimit(count);
        } else {
            return reviewsStorage.findAllByFilmIdWithLimit(id, count);
        }
    }

    @Override
    public Reviews addLike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, INCREMENT_ON_LIKE, true);
    }

    @Override
    public Reviews addDislike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, -DECREMENT_ON_DISLIKE, false);
    }

    @Override
    public Reviews removeLike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, -DECREMENT_ON_DISLIKE, null);
    }

    @Override
    public Reviews removeDislike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, INCREMENT_ON_LIKE, null);
    }

    public Reviews changeLikeStatus(Long reviewsId, Long userId, Long incrementValue, Boolean isLike) {
        var reviews = reviewsStorage.findById(reviewsId);
        var currentUseful = reviews.getUseful();
        var updatedUseful = currentUseful + incrementValue;

        long newUsefulValue;
        if (isLike != null) {
            newUsefulValue = (updatedUseful == 0) ? -1 : updatedUseful;
        } else {
            newUsefulValue = updatedUseful;
        }

        reviews.setUseful(newUsefulValue);

        if (isLike != null) {
            reviewsLikeStorage.update(reviewsId, userId, isLike);
        } else {
            reviewsLikeStorage.delete(reviewsId, userId);
        }

        return reviewsStorage.update(reviews);
    }
}
