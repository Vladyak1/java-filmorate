package ru.yandex.practicum.filmorate.storage.reviews;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Reviews;

import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class ReviewsDbStorage implements ReviewsStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Reviews> rowMapper = (rs, rowNum) -> new Reviews(
            rs.getLong("id"),
            rs.getLong("useful"),
            rs.getString("content"),
            rs.getBoolean("isPositive"),
            rs.getLong("user_id"),
            rs.getLong("film_id")
    );
    private final String insertSql = """
            INSERT INTO reviews
            (content, isPositive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?)
            """;
    private final String updateSql = """
            UPDATE reviews SET
            content = ?,
            isPositive = ?,
            user_id = ?,
            film_id = ?,
            useful = ?
            WHERE id = ?
            """;
    private final String deleteSqlById = """
            DELETE FROM reviews
            WHERE id = ?
            """;
    private final String selectSqlById = """
            SELECT * FROM reviews
            WHERE id = ?
            """;
    private final String selectAllWithLimit = """
            SELECT * FROM reviews
            ORDER BY useful DESC
            LIMIT ?
            """;
    private final String selectAllByFilmIdWithLimit = """
            SELECT * FROM reviews
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?
            """;

    @Override
    public Reviews save(Reviews entity) {
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(insertSql, new String[]{"id"});
            ps.setString(1, entity.getContent());
            ps.setBoolean(2, entity.getIsPositive());
            ps.setLong(3, entity.getUserId());
            ps.setLong(4, entity.getFilmId());
            ps.setLong(5, entity.getUseful());
            return ps;
        }, keyHolder);

        var id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        entity.setReviewId(id);

        return entity;
    }

    @Override
    public Reviews update(Reviews entity) {
        try {
            jdbcTemplate.update(
                    updateSql,
                    entity.getContent(),
                    entity.getIsPositive(),
                    entity.getUserId(),
                    entity.getFilmId(),
                    entity.getUseful(),
                    entity.getReviewId()
            );
        } catch (DataAccessException ex) {
            throw new RuntimeException("Обновление отзыва не удалось.", ex);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        try {
            jdbcTemplate.update(deleteSqlById, id);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Удаление отзыва не удалось.", ex);
        }
    }

    @Override
    public Reviews findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(selectSqlById, rowMapper, id);
        } catch (DataAccessException ex) {
            throw new NotFoundException("Отзыв с id " + id + " не существует.");
        }
    }

    @Override
    public List<Reviews> findAllWithLimit(Integer count) {
        return jdbcTemplate.query(selectAllWithLimit, rowMapper, count);
    }

    @Override
    public List<Reviews> findAllByFilmIdWithLimit(Long id, Integer count) {
        return jdbcTemplate.query(selectAllByFilmIdWithLimit, rowMapper, id, count);
    }
}