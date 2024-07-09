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
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

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
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        final String sql = """
                INSERT INTO films (name, release_date, description, duration, rating_mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

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
        final String sql = """
                UPDATE films
                SET name = ?, release_date = ?, description = ?, duration = ?, rating_mpa_id = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, film.getName(), film.getReleaseDate(), film.getDescription(),
                film.getDuration(), film.getMpa().getId(), film.getId());

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
        directorStorage.deleteDirectorFromFilm(film.getId());
        addDirectors(film.getId(), film.getDirectors());
        return findFilm(film.getId());
    }

    @Override
    public List<Film> getFilms() {
        final String sql = """
                SELECT f.*, mr.name mpa_name
                FROM films f
                JOIN mpa_ratings mr on f.rating_mpa_id = mr.id
                ORDER BY f.id
                """;
        return jdbcTemplate.query(sql, filmRowMapper());
    }

    @Override
    public void addLike(long filmId, long userId) {
        final String sql = """
                MERGE INTO film_likes (user_id, film_id)
                VALUES (?, ?)
                """;
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public void delLike(long filmId, long userId) {
        final String sql = """
                DELETE FROM film_likes
                WHERE user_id = ? and film_id = ?
                """;
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(long count, Integer genreId, Integer year) {
        String sql = "SELECT f.*, mr.name as mpa_name, " +
                "(SELECT COUNT(fl.user_id) FROM film_likes fl WHERE fl.film_id = f.id) as like_count " +
                "FROM films f " +
                "JOIN mpa_ratings mr on f.rating_mpa_id = mr.id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "WHERE (:genreId IS NULL OR fg.genre_id = :genreId) " +
                "AND (:year IS NULL OR YEAR(f.release_date) = :year) " +
                "GROUP BY f.id, mr.name " +
                "ORDER BY like_count DESC " +
                "LIMIT :count";

        Map<String, Object> params = new HashMap<>();
        params.put("genreId", genreId);
        params.put("year", year);
        params.put("count", count);

        return namedParameterJdbcTemplate.query(sql, params, filmRowMapper());
    }


    @Override
    public Film findFilm(Long id) {
        Film result;
        final String sql = """
                SELECT f.*, mr.name mpa_name
                FROM films f
                JOIN mpa_ratings mr on f.rating_mpa_id = mr.id
                WHERE f.id = ?
                """;
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
        final String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;
        jdbcTemplate.update(sql, filmId, genreId);
    }

    @Override
    public List<Genre> getFilmGenres(Long filmId) {
        final String sql = """
                SELECT distinct g.id as id, g.name
                FROM film_genres fg
                LEFT JOIN genres g on fg.genre_id = g.id
                WHERE film_id = ?
                """;
        return jdbcTemplate.query(sql, genreRowMapper(), filmId);
    }

    @Override
    public void delFilmGenres(Long filmId) {
        final String sql = """
                DELETE FROM film_genres
                WHERE film_id = ?
                """;
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public Mpa getFilmMpa(Long filmId) {
        final String sql = """
                SELECT mr.id, mr.name from films f
                LEFT JOIN mpa_ratings mr on f.rating_mpa_id = mr.id
                WHERE f.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, mpaRowMapper(), filmId);
    }

    @Override
    public void delFilmMpa(Long filmId) {
        final String sql = """
                UPDATE films
                SET rating_mpa_id = null
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public List<Film> getDirectorFilmsSorted(long directorId, String sort) {
        String sql = """
                SELECT * from film_director as fd
                JOIN films as f ON fd.film_id=f.id
                JOIN mpa_ratings on f.rating_mpa_id = mpa_ratings.id
                LEFT JOIN film_likes as l on f.id = l.film_id
                WHERE fd.director_id= :directorId
                GROUP BY f.id, f.release_date
                ORDER BY case when :sort = 'year' then f.release_date end,
                CASE WHEN :sort = 'sort' then count(l.film_id) end desc
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("directorId", directorId)
                .addValue("sort", sort);

        return namedParameterJdbcTemplate.query(sql, params,
                (rs, rowNum) -> Film.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .mpa(new Mpa(rs.getInt("rating_mpa_id"), rs.getString("mpa_ratings.name")))
                        .genres(getFilmGenres(rs.getLong("id")))
                        .directors(getDirectorsByFilmId(rs.getLong("id")))
                        .build());

//
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
                    (:searchByTitle = TRUE AND LOWER(films.name) LIKE :textForSearch)
                    OR (:searchByDirector = TRUE AND LOWER(directors.director_name) LIKE :textForSearch)
                GROUP BY
                    films.id
                ORDER BY
                    likes DESC
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("textForSearch", "%" + textForSearch.toLowerCase() + "%");
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

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        String sql = """
                SELECT f.*,
                       mr.*,
                       count(fl.film_id)
                FROM film_likes AS fl
                JOIN films AS f ON fl.film_id = f.id
                LEFT JOIN mpa_ratings AS mr ON f.rating_mpa_id = mr.id
                WHERE fl.user_id = :userId
                  AND fl.film_id in
                    (SELECT film_id
                     FROM film_likes
                     WHERE user_id = :friendId)
                GROUP BY f.id
                ORDER BY count(fl.film_id) DESC
                """;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        parameters.addValue("friendId", friendId);

        return namedParameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new Mpa(rs.getInt("rating_mpa_id"), rs.getString("mpa_ratings.name")));
            film.setGenres(getFilmGenres(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
            return film;
        });
    }

    private RowMapper<Film> filmRowMapper() {
        return ((rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new Mpa(rs.getInt("rating_mpa_id"),
                    rs.getString("mpa_ratings.name")));
            film.setGenres(getFilmGenres(rs.getLong("id")));
            film.setDirectors(getDirectorsByFilmId(rs.getLong("id")));
            return film;
        });
    }

    private RowMapper<Genre> genreRowMapper() {
        return ((rs, rowNum) -> new Genre(
                rs.getInt("id"),
                rs.getString("name")));
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return ((rs, rowNum) -> new Mpa(
                rs.getInt("id"),
                rs.getString("name")));
    }

    private void addDirectors(long film_id, List<Director> directors) {
        if (!CollectionUtils.isEmpty(directors)) {
            directorStorage.addDirectorsToFilm(film_id, directors);
        }
    }
}