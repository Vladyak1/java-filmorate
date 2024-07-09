package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.service.reviews.ReviewsService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final ReviewsService reviewsService;

    @PostMapping
    public Reviews createReview(@RequestBody @Valid Reviews review) {
        log.info("Получен запрос на создание отзыва");
        return reviewsService.createReview(review);
    }

    @PutMapping
    public Reviews updateReview(@RequestBody @Valid Reviews review) {
        log.info("Получен запрос на обновление отзыва id {}", review.getReviewId());
        return reviewsService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable long id) {
        log.info("Получен запрос на удаление отзыва id {}", id);
        reviewsService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Reviews getReviewById(@PathVariable Long id) {
        log.info("Получен запрос на получение отзыва id {}", id);
        return reviewsService.getReviewById(id);
    }

    @GetMapping
    public List<Reviews> getAllReviews(@RequestParam(value = "filmId", required = false) Long filmId,
                                            @RequestParam(value = "count", defaultValue = "10") Integer count) {
        log.info("Получен запрос на получение всех отзывов, параметры отбора: filmId {}, count {}", filmId, count);
        return reviewsService.getAllReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Reviews addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление like");
        return reviewsService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Reviews addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление dislike");
        return reviewsService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Reviews removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление like");
        return reviewsService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Reviews removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление dislike");
        return reviewsService.removeDislike(id, userId);
    }

}

