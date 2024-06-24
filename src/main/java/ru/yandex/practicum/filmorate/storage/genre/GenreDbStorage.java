package ru.yandex.practicum.filmorate.storage.genre;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private JdbcTemplate jdbcTemplate;
    private final String sql = "select * from genres";

    @Override
    public Genre getGenreById(int id) {
        String query = sql + " where id = ?";
        try {
            return jdbcTemplate.queryForObject(query, genreRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Не удалось вернуть рейтинг жанр c id: {}.", id);
            throw new NotFoundException("Не удалось вернуть жанр.");
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query(sql, genreRowMapper());
    }

    private RowMapper<Genre> genreRowMapper() {
        return ((rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }
}