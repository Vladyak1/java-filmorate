package ru.yandex.practicum.filmorate.storage.user;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class UserDbStorage implements UserStorage {
    private JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {
        final String sql = "insert into users (name, login, birthday, email) values (?, ?, ?, ?)";
        KeyHolder gkh = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setObject(3, user.getBirthday());
            ps.setString(4, user.getEmail());
            return ps;
        }, gkh);
        Long id = Objects.requireNonNull(gkh.getKey()).longValue();
        user.setId(id);
        if (user.getName().isEmpty())
            user.setName(user.getLogin());
        return user;
    }

    @Override
    public void delUser(long id) {
        final String sql = "delete from users where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void delAllUsers() {
        final String sql = "delete from users";
        jdbcTemplate.update(sql);
    }

    @Override
    public User updUser(User user) {
        final String sql = "update users set name = ?, login = ?, birthday = ?, email = ? where id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getBirthday(), user.getEmail(), user.getId());

        return user;
    }

    @Override
    public List<User> getUsers() {
        final String sql = "select * from users";
        return jdbcTemplate.query(sql, userRowMapper());
    }

    @Override
    public void addFriend(long id, long friendId) {
        final String sql = "insert into friends (user_id_1, user_id_2) values (?, ?)";
        jdbcTemplate.update(sql, id, friendId);
    }

    @Override
    public void delFriend(long id, long friendId) {
        final String sql = "delete from friends where user_id_1 = ? and user_id_2 = ?";
        jdbcTemplate.update(sql, id, friendId);
    }

    @Override
    public List<User> getFriends(long id) {
        final String sql = "select * from users u, friends f " +
                "where u.id = f.user_id_2 and f.user_id_1 = ?";
        return jdbcTemplate.query(sql, userRowMapper(), id);
    }

    @Override
    public List<User> getCommonFriends(long id, long friendId) {
        final String sql = "select * from users u, friends f, friends o " +
                "where u.id = f.user_id_2 and u.id = o.user_id_2 and f.user_id_1 = ? and o.user_id_1 = ?";
        return jdbcTemplate.query(sql, userRowMapper(), id, friendId);
    }

    @Override
    public User findUser(Long id) {
        User result;
        final String sql = "select * from users where id = ?";
        try {
           result = jdbcTemplate.queryForObject(sql, userRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(e.getMessage());
        }
        return result;
    }

    private RowMapper<User> userRowMapper() {
        return ((rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("login"),
                Objects.requireNonNull(rs.getDate("birthday")).toLocalDate(),
                rs.getString("email")
        ));
    }
}