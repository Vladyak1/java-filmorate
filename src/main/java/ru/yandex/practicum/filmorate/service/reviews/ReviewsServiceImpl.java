package ru.yandex.practicum.filmorate.service.reviews;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewsStorage;
import ru.yandex.practicum.filmorate.storage.reviews.like.ReviewsLikeDbStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsServiceImpl implements ReviewsService {
    private final ReviewsStorage reviewsStorage;
    private final ReviewsLikeDbStorage reviewsLikeStorage;
    private final EventStorage eventStorage;
    private final long incrementOnLike = 1;
    private final long decrementOnDislike = 1;

    @Override
    public Reviews createReview(Reviews reviews) {
        if (reviews.getFilmId() < 0 || reviews.getUserId() < 0) {
            throw new NotFoundException("Id не может быть отрицательным");
        }
        Reviews newReview = reviewsStorage.save(reviews);
        eventStorage.addEvent(Event.builder()
                .userId(newReview.getUserId())
                .entityId(newReview.getReviewId())
                .eventType(EventType.REVIEW)
                .operation(Operation.ADD)
                .build());
        return newReview;
    }

    @Override
    public Reviews updateReview(Reviews reviews) {
        Reviews updatedReview = reviewsStorage.update(reviews);
        eventStorage.addEvent(Event.builder()
                .userId(updatedReview.getUserId())
                .entityId(updatedReview.getReviewId())
                .eventType(EventType.REVIEW)
                .operation(Operation.UPDATE)
                .build());
        return updatedReview;
    }

    @Override
    public void deleteReview(Long id) {
        eventStorage.addEvent(Event.builder()
                .userId(reviewsStorage.findById(id).getUserId())
                .entityId(id)
                .eventType(EventType.REVIEW)
                .operation(Operation.REMOVE)
                .build());
        reviewsStorage.delete(id);
    }

    @Override
    public Reviews getReviewById(Long id) {
        return reviewsStorage.findById(id);
    }

    @Override
    public List<Reviews> getAllReviews(Long id, Integer count) {
        return Optional.ofNullable(id)
                .map(idValue -> reviewsStorage.findAllByFilmIdWithLimit(idValue, count))
                .orElseGet(() -> reviewsStorage.findAllWithLimit(count));
    }

    @Override
    public Reviews addLike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, incrementOnLike, true);
    }

    @Override
    public Reviews addDislike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, -decrementOnDislike, false);
    }

    @Override
    public Reviews removeLike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, -decrementOnDislike, null);
    }

    @Override
    public Reviews removeDislike(Long reviewsId, Long userId) {
        return changeLikeStatus(reviewsId, userId, incrementOnLike, null);
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

        return reviewsStorage.updateUseful(reviews);
    }
}
