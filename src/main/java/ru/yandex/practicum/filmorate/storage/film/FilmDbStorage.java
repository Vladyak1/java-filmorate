package ru.yandex.practicum.filmorate.storage.film;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        final String sql = "insert into films (name, release_date, description, duration, rating_mpa_id) " +
                "values (?, ?, ?, ?, ?)";

        KeyHolder gkh = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setObject(2, film.getReleaseDate());
            ps.setString(3, film.getDescription());
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());

            return ps;
        }, gkh);
        film.setId(Objects.requireNonNull(gkh.getKey()).longValue());

        final String sql1 = "insert into film_genres (film_id, genre_id) values (?, ?)";
        final List<Genre> genreList = new ArrayList<>(new HashSet<>(film.getGenres()));
        jdbcTemplate.batchUpdate(
                sql1,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genreList.get(i).getId());
                    }

                    public int getBatchSize() {
                        return genreList.size();
                    }
                });

        return film;
    }

    @Override
    public void delFilm(Long id) {
        final String sql = "delete from films where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void delAllFilms() {
        final String sql = "delete from films";
        jdbcTemplate.update(sql);
    }

    @Override
    public Film updFilm(Film film) {
        final String sql = "update films set name = ?, release_date = ?, description = ?, duration = ? " +
                "where id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getReleaseDate(), film.getDescription(),
                film.getDuration(), film.getId());

        jdbcTemplate.update("delete from FILM_GENRES where FILM_ID = ?", film.getId());

        final List<Genre> genreList = new ArrayList<>(new HashSet<>(film.getGenres()));
        jdbcTemplate.batchUpdate(
                "insert into film_genres (film_id, genre_id) values (?, ?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genreList.get(i).getId());
                    }

                    public int getBatchSize() {
                        return genreList.size();
                    }
                });
        return film;
    }

    @Override
    public List<Film> getFilms() {
        final String sql = "select f.*, mr.name mpa_name from films f join mpa_ratings mr on f.rating_mpa_id = mr.id " +
                "order by f.id";
        return jdbcTemplate.query(sql, filmRowMapper());
    }

    @Override
    public void addLike(long filmId, long userId) {
        final String sql = "insert into film_likes (user_id, film_id) values (?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public void delLike(long filmId, long userId) {
        final String sql = "delete from film_likes where user_id = ? and film_id = ?";
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(long count) {
        final String sql = "select f.*, mr.name mpa_name from films f " +
                "left join film_likes fl on f.id = fl.film_id join mpa_ratings mr on f.rating_mpa_id = mr.id " +
                "group by f.name, f.id, mr.name order by count(fl.film_id) desc limit ?";
        return jdbcTemplate.query(sql, filmRowMapper(), count);
    }

    @Override
    public Film findFilm(Long id) {
        Film result;
        final String sql = "select f.*, mr.name mpa_name from films f join mpa_ratings mr on f.rating_mpa_id = mr.id " +
                "where f.id = ?";
        try {
            result = jdbcTemplate.queryForObject(sql, filmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException(e.getCause());
        }
        assert result != null;
        result.setGenres(getFilmGenres(id));
        return result;
    }

    @Override
    public void addFilmGenre(Long filmId, Integer genreId) {
        final String sql = "insert into film_genres (film_id, genre_id) values (?, ?)";

        jdbcTemplate.update(sql, filmId, genreId);
    }

    @Override
    public List<Genre> getFilmGenres(Long filmId) {
        final String sql = "select distinct g.id as id, g.name from film_genres fg left join genres g on " +
                "fg.genre_id = g.id where film_id = ?";
        return jdbcTemplate.query(sql, genreRowMapper(), filmId);
    }

    @Override
    public void delFilmGenres(Long filmId) {
        final String sql = "delete from film_genres where film_id = ?";

        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public Mpa getFilmMpa(Long filmId) {
        final String sql = "select mr.id, mr.name from films f " +
                "left join mpa_ratings mr on f.rating_mpa_id = mr.id where f.id = ?";

        return jdbcTemplate.queryForObject(sql, mpaRowMapper(), filmId);
    }

    @Override
    public void delFilmMpa(Long filmId) {
        final String sql = "update films set rating_mpa_id = null where id = ?";

        jdbcTemplate.update(sql, filmId);
    }

    private RowMapper<Film> filmRowMapper() {
        return ((rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new Mpa(rs.getInt("rating_mpa_id"), rs.getString("mpa_name")));

            return film;
        });
    }

    private RowMapper<Genre> genreRowMapper() {
        return ((rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return ((rs, rowNum) -> new Mpa(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }
}