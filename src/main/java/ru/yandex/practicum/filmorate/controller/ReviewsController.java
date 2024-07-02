package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Reviews;
import ru.yandex.practicum.filmorate.service.reviews.ReviewsService;

import javax.validation.Valid;

/**
 * Класс {@code ReviewsController} обрабатывает HTTP запросы для создания нового отзыва.
 */
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final ReviewsService reviewsService;

    /**
     * Добавление нового отзыва.
     *
     * @param review Объект отзыва в теле запроса, который необходимо добавить
     * @return Добавленный отзыв (тип: {@link Reviews})
     */
    @PostMapping
    public Reviews createReview(@RequestBody @Valid Reviews review) {
        log.info("Получен запрос на создание отзыва");
        return reviewsService.createReview(review);
    }

    /**
     * Обновление существующего отзыва.
     *
     * @param review Объект отзыва в теле запроса, который необходимо обновить
     * @return Обновленный отзыв (тип: {@link Reviews})
     */
    @PutMapping
    public Reviews updateReview(@RequestBody @Valid Reviews review) {
        log.info("Получен запрос на обновление отзыва id {}", review.getReviewId());
        return reviewsService.updateReview(review);
    }

    /**
     * Удаление существующего отзыва по ID отзыва.
     *
     * @param id ID отзыва, который необходимо удалить
     */
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable long id) {
        log.info("Получен запрос на удаление отзыва id {}", id);
        reviewsService.deleteReview(id);
    }

    /**
     * Получение отзыва по ID отзыва.
     *
     * @param id ID отзыва, который необходимо получить
     * @return Отзыв с указанным ID отзыва (тип: {@link Reviews})
     */
    @GetMapping("/{id}")
    public Reviews getReviewById(@PathVariable Long id) {
        log.info("Получен запрос на получение отзыва id {}", id);
        return reviewsService.getReviewById(id);
    }
}
