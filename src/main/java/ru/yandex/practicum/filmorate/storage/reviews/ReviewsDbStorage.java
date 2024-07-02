package ru.yandex.practicum.filmorate.storage.reviews;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Reviews;

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

    private final String INSERT_SQL = """
            insert into reviews
            (content, isPositive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final String UPDATE_SQL = """
            UPDATE reviews SET
            content = ?,
            isPositive = ?,
            user_id = ?,
            film_id = ?,
            useful = ?
            WHERE id = ?
            """;

    private final String DELETE_SQL_BY_ID = """
            DELETE FROM reviews
            WHERE id = ?
            """;

    private final String SELECT_SQL_BY_ID = """
            SELECT * FROM reviews
            WHERE id = ?
            """;

    @Override
    public Reviews save(Reviews entity) {
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
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
                    UPDATE_SQL,
                    entity.getContent(),
                    entity.getIsPositive(),
                    entity.getUserId(),
                    entity.getFilmId(),
                    entity.getUseful(),
                    entity.getReviewId()
            );
        } catch (DataAccessException ex) {
            throw new RuntimeException("Удаление отзыва не удалось.", ex);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        try {
            jdbcTemplate.update(DELETE_SQL_BY_ID, id);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to delete the review.", ex);
        }
    }

    @Override
    public Reviews findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(SELECT_SQL_BY_ID, rowMapper, id);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Поиск отзыва не удался.", ex);
        }
    }
}