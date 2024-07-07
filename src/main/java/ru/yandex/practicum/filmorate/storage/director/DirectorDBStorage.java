package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class DirectorDBStorage implements DirectorStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DirectorMapper directorMapper;

    @Override
    public List<Director> getDirectors() {
        try {
            return jdbcTemplate.query("select * from directors", directorMapper);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<Director> getDirectorById(long id) {
        String sql = "select * from directors where director_id = :id";
        try {
            Director director = jdbcTemplate.queryForObject(sql, Map.of("id", id), directorMapper);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Director saveDirector(Director director) {
        director.setId(null);
        String sql = "insert into directors (director_name) values (:name)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, new MapSqlParameterSource("name", director.getName()), keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return director;
    }

    @Override
    public void deleteDirector(long id) {
        jdbcTemplate.update("delete from directors where director_id = :id", Map.of("id", id));
    }

    @Override
    public void addDirectorsToFilm(long filmId, List<Director> directors) {
        String sql = "insert into film_director (film_id, director_id) values (?, ?)";
        jdbcTemplate.getJdbcOperations().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, directors.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "update directors set director_name = :name where director_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", director.getName())
                .addValue("id", director.getId());
        jdbcTemplate.update(sql, params);
        return director;
    }
}
