package ru.yandex.practicum.filmorate.storage.reviews.like;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReviewsLikeDbStorage implements ReviewsLikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final String insertReviewLike = """
            INSERT INTO reviews_likes (reviews_id, user_id, isLike) VALUES (?, ?, ?)
            """;
    private final String deleteReviewLike = """
            DELETE FROM reviews_likes
            WHERE reviews_id = ? AND user_id = ?
            """;
    private final String updateReviewLike = """
            UPDATE reviews_likes SET
            reviews_id = ?,
            user_id = ?,
            isLike = ?
            WHERE reviews_id = ? AND user_id = ?
            """;

    @Override
    public void save(Long reviewsId, Long userId, Boolean isLike) {
        try {
            jdbcTemplate.update(insertReviewLike, reviewsId, userId, isLike);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Добавление рейтинга отзыва не удалось.", ex);
        }
    }

    @Override
    public void delete(Long reviewsId, Long userId) {
        try {
            jdbcTemplate.update(deleteReviewLike, reviewsId, userId);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Удаление рейтинга отзыва не удалось.", ex);
        }
    }

    @Override
    public void update(Long reviewsId, Long userId, Boolean isLike) {
        try {
            jdbcTemplate.update(updateReviewLike, reviewsId, userId, isLike, reviewsId, userId);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Обновление рейтинга отзыва не удалось.", ex);
        }
    }
}
