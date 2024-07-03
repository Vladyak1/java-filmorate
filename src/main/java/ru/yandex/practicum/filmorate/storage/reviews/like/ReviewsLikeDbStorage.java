package ru.yandex.practicum.filmorate.storage.reviews.like;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReviewsLikeDbStorage implements ReviewsLikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final String INSERT_REVIEW_LIKE = """
            INSERT INTO reviews_likes (reviews_id, user_id, isLike) VALUES (?, ?, ?)
            """;
    private final String DELETE_REVIEW_LIKE = """
            DELETE FROM reviews_likes
            WHERE reviews_id = ? AND user_id = ?
            """;
    private final String UPDATE_REVIEW_LIKE = """
            UPDATE reviews_likes SET
            reviews_id = ?,
            user_id = ?,
            isLike = ?
            WHERE reviews_id = ? AND user_id = ?
            """;

    @Override
    public void save(Long reviewsId, Long userId, Boolean isLike) {
        jdbcTemplate.update(INSERT_REVIEW_LIKE, reviewsId, userId, isLike);
    }

    @Override
    public void delete(Long reviewsId, Long userId) {
        try {
            jdbcTemplate.update(DELETE_REVIEW_LIKE, reviewsId, userId);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Удаление отзыва не удалось.", ex);
        }
    }

    @Override
    public void update(Long reviewsId, Long userId, Boolean isLike) {
        jdbcTemplate.update(UPDATE_REVIEW_LIKE, reviewsId, userId, isLike, reviewsId, userId);
    }
}
