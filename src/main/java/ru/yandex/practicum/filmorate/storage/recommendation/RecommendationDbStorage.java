package ru.yandex.practicum.filmorate.storage.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecommendationDbStorage implements RecommendationStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlGetRecommenderUser = "SELECT f2.user_id " +
            "FROM film_likes f1 " +
            "JOIN film_likes f2 ON f1.film_id = f2.film_id " +
            "AND f1.user_id != f2.user_id " +
            "WHERE f1.user_id = :userId " +
            "GROUP BY f2.user_id " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT 1";
    private final String sqlGetRecommendations = "SELECT f.id, f.name, f.description, f.release_date, f.duration " +
            "FROM films f " +
            "JOIN film_likes fl ON f.id = fl.film_id " +
            "WHERE fl.user_id = :similarUserId " +
            "AND f.id NOT IN (" +
            "SELECT film_id " +
            "FROM film_likes " +
            "WHERE user_id = :userId" +
            ") ";

    @Override
    public List<Film> getRecommendations(long userId) {
        log.info("Вызов метода getRecommendations() c userId = {}", userId);
        Optional<Long> similarUserId = getRecommenderUser(userId);
        if (similarUserId.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbc.query(sqlGetRecommendations, Map.of("userId", userId, "similarUserId", similarUserId.get()),
                (rs, rowNum) -> Film.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .build());
    }

    private Optional<Long> getRecommenderUser(long userId) {
        log.info("Вызов метода getRecommenderUser() c userId = {}", userId);
        try {
            return Optional.ofNullable(jdbc.queryForObject(sqlGetRecommenderUser,
                    Map.of("userId", userId), Long.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
