package ru.yandex.practicum.filmorate.storage.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecommendationDbStorage implements RecommendationStorage {
    private final NamedParameterJdbcTemplate jdbc;
    private final FilmStorage filmStorage;
    private static final String SQL_GET_RECOMMENDER_USER = """
            SELECT f2.user_id
            FROM film_likes f1
            JOIN film_likes f2 ON f1.film_id = f2.film_id AND f1.user_id != f2.user_id
            WHERE f1.user_id = :userId
            GROUP BY f2.user_id
            ORDER BY COUNT(*) DESC
            LIMIT 1
            """;
    private static final String SQL_GET_RECOMMENDATIONS = """
            SELECT *
            FROM films f
            JOIN film_likes fl ON f.id = fl.film_id
            JOIN mpa_ratings as mr ON f.rating_mpa_id = mr.id
            WHERE fl.user_id = :similarUserId
            AND f.id NOT IN (
                SELECT film_id
                FROM film_likes
                WHERE user_id = :userId
                )
            """;

    @Override
    public List<Film> getRecommendations(long userId) {
        log.info("Вызов метода getRecommendations() c userId = {}", userId);
        Optional<Long> similarUserId = getRecommenderUser(userId);
        if (similarUserId.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbc.query(SQL_GET_RECOMMENDATIONS, Map.of("userId", userId, "similarUserId", similarUserId.get()),
                (rs, rowNum) -> Film.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .mpa(new Mpa(rs.getInt("rating_mpa_id"), rs.getString("mpa_ratings.name")))
                        .genres(filmStorage.getFilmGenres(rs.getLong("id")))
                        .directors(filmStorage.getDirectorsByFilmId(rs.getLong("id")))
                        .build());
    }

    private Optional<Long> getRecommenderUser(long userId) {
        log.info("Вызов метода getRecommenderUser() c userId = {}", userId);
        try {
            return Optional.ofNullable(jdbc.queryForObject(SQL_GET_RECOMMENDER_USER,
                    Map.of("userId", userId), Long.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
