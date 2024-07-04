package ru.yandex.practicum.filmorate.storage.film;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmSortRowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private JdbcTemplate jdbcTemplate;
    private final GenreService genreService;
    private final DirectorStorage directorStorage;
    private final FilmSortRowMapper sortRowMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
        Set<Integer> genresIds = genreService.getAllGenres()
                .stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Set<Integer> filmGenresIds = film.getGenres()
                .stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Set<Integer> incorrectGenreIds = filmGenresIds
                .stream()
                .filter(genreId -> !genresIds.contains(genreId))
                .collect(Collectors.toSet());
        if (!incorrectGenreIds.isEmpty()) {
            log.warn("Жанры по следующим id не найдены: {}", incorrectGenreIds);
            throw new IllegalArgumentException("Жанры по следующим id не найдены: " + incorrectGenreIds);
        }
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
        addDirectors(film.getId(), film.getDirectors());

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
    public List<Film> getPopularFilms(long count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, mr.name mpa_name FROM films f " +
                        "JOIN film_genres fg ON f.id = fg.film_id " +
                        "JOIN mpa_ratings mr on f.rating_mpa_id = mr.id " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id "
        );

        List<Object> params = new ArrayList<>();

        boolean hasGenre = genreId != null;
        boolean hasYear = year != null;

        if (hasGenre || hasYear) {
            sql.append("WHERE ");
        }

        if (hasGenre) {
            sql.append("fg.genre_id = ? ");
            params.add(genreId);
        }

        if (hasGenre && hasYear) {
            sql.append("AND ");
        }

        if (hasYear) {
            sql.append("YEAR(f.release_date) = ? ");
            params.add(year);
        }

        sql.append("GROUP BY f.id, mr.name ");
        sql.append("ORDER BY COUNT(fl.user_id) DESC ");
        sql.append("LIMIT ?");
        params.add(count);

        return jdbcTemplate.query(sql.toString(), filmRowMapper(), params.toArray());
    }

    @Override
    public Film findFilm(Long id) {
        Film result;
        final String sql = "select f.*, mr.name mpa_name from films f join mpa_ratings mr on f.rating_mpa_id = mr.id " +
                           "where f.id = ?";
        try {
            result = jdbcTemplate.queryForObject(sql, filmRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(e.getMessage());
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

    @Override
    public List<Film> getDirectorFilmsSorted(long directorId, String sort) {
        String sql = "";
        switch (sort) {
            case "year":
                sql = "select * from films as f join mpa_ratings as mr on f.rating_mpa_id = mr.id " +
                      "left join film_director as fd on f.id = fd.film_id " +
                      "join directors as d on fd.director_id = d.director_id " +
                      "where f.id in (select fd.film_id from film_director as fd where director_id = ?)" +
                      "order by f.release_date";
                break;
            case "likes":
                sql = "select * from films as f " +
                      "join mpa_ratings as mr on f.rating_mpa_id = mr.id " +
                      "left join film_director as fd on f.id = fd.film_id " +
                      "join directors as d on fd.director_id = d.director_id " +
                      "where f.id in (select fd.film_id from film_director as fd where director_id = ?) " +
                      "and f.id in (select f.id from films as f left join film_likes as l on f.id = l.film_id " +
                      "group by f.id order by count(l.film_id) desc)";

                break;

        }
        return jdbcTemplate.query(sql, sortRowMapper, directorId);
    }

    @Override
    public List<Film> getFilmListBySearch(String textForSearch, Boolean searchByDirector, Boolean searchByTitle) {
        String sql = """
                SELECT
                    films.*,
                    mpa_ratings.id AS rating_mpa_id,
                    mpa_ratings.name AS mpa_name,
                    COUNT(film_likes.film_id) as likes
                FROM films
                    LEFT JOIN mpa_ratings
                    ON films.rating_mpa_id = mpa_ratings.id
                    LEFT JOIN film_director
                    ON films.id = film_director.film_id
                    LEFT JOIN directors
                    ON film_director.director_id = directors.director_id
                    LEFT JOIN film_likes
                    ON films.id = film_likes.film_id
                WHERE
                    (:searchByTitle = TRUE AND films.name LIKE :textForSearch)
                    OR (:searchByDirector = TRUE AND directors.director_name LIKE :textForSearch)
                GROUP BY
                    films.id
                ORDER BY
                    likes DESC
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("textForSearch", "%" + textForSearch + "%");
        parameters.addValue("searchByTitle", searchByTitle);
        parameters.addValue("searchByDirector", searchByDirector);

        return namedParameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new Mpa(rs.getInt("rating_mpa_id"), rs.getString("mpa_name")));
            film.setGenres(getFilmGenres(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
            return film;
        });
    }

    @Override
    public List<Director> getDirectorsByFilmId(Long filmId) {
        String sql = """
                SELECT
                    d.director_id,
                    d.director_name
                FROM directors AS d
                    INNER JOIN film_director fd
                    ON fd.director_id = d.director_id
                WHERE
                    fd.film_id = :filmId
                """;

        var parameters = new MapSqlParameterSource();
        parameters.addValue("filmId", filmId);

        return namedParameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> new Director(
                rs.getLong("director_id"),
                rs.getString("director_name")
        ));
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

    private void addDirectors(long film_id, List<Director> directors) {
        if (!directors.isEmpty()) {
            directorStorage.addDirectorsToFilm(film_id, directors);
        }

    }
}