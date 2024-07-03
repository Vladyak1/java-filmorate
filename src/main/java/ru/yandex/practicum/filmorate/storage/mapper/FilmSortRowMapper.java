package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FilmSortRowMapper implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, Film> filmMap = new LinkedHashMap<>();
        while (rs.next()) {
            String filmId = rs.getString("id");
            filmMap.putIfAbsent(filmId,
                    Film.builder()
                            .id(rs.getLong("id"))
                            .name(rs.getString("name"))
                            .description(rs.getString("description"))
                            .releaseDate(rs.getDate("release_date").toLocalDate())
                            .duration(rs.getInt("duration"))
                            .mpa(new Mpa(rs.getInt("rating_mpa_id"), rs.getString("mpa_ratings.name")))
                            .directors(new ArrayList<>())
                            .build());
            filmMap.get(filmId).getDirectors()
                    .add(new Director(rs.getLong("directors.director_id"),
                            rs.getString("directors.director_name")));
        }
        return new ArrayList<>(filmMap.values());
    }
}
