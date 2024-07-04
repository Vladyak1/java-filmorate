package ru.yandex.practicum.filmorate.storage.reviews.like;

public interface ReviewsLikeStorage {

    void save(Long reviewsId, Long userId, Boolean isLike);

    void delete(Long reviewsId, Long userId);

    void update(Long reviewsId, Long userId, Boolean isLike);
}
