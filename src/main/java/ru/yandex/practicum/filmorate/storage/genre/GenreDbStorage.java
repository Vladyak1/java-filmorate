package ru.yandex.practicum.filmorate.storage.genre;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private JdbcTemplate jdbcTemplate;
    private final String sql = "select * from genres";

    @Override
    public Optional<Genre> getGenreById(int id) {
        final String sql1 = sql + " where id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql1, genreRowMapper(), id));
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