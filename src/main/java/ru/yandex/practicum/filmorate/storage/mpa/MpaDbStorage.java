package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
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
    public Mpa getMpaById(int id) {
        String query = sql + " where id = ?";
        try {
            return jdbcTemplate.queryForObject(query, mpaRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Не удалось вернуть рейтинг MPA c id: {}.", id);
            throw new NotFoundException("Не удалось вернуть рейтинг MPA.");
        }
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return ((rs, rowNum) -> new Mpa(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }
}