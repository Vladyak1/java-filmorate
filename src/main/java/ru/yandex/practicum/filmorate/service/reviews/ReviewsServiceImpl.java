package ru.yandex.practicum.filmorate.service.reviews;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.storage.reviews.ReviewsStorage;

/**
 * Класс {@code ReviewsServiceImpl} реализует интерфейс {@link ReviewsService}
 * и предоставляет методы для работы с отзывами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewsServiceImpl implements ReviewsService {
    private final ReviewsStorage reviewsStorage;

    @Override
    public Reviews createReview(Reviews reviews) {
        if (reviews.getFilmId() < 0 ) {
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
}
