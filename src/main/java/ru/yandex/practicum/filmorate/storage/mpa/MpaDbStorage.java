package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private JdbcTemplate jdbcTemplate;
    private final String sql = "select * from mpa_ratings";

    @Override
    public List<Mpa> getAllMpa() {
        return jdbcTemplate.query(sql, mpaRowMapper());
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        final String sql1 = sql + " where id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql1, mpaRowMapper(), id));
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return ((rs, rowNum) -> new Mpa(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }
}