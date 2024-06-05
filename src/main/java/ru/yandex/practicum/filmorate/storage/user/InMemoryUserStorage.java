package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> getAllUsers() {
        log.info("Количество добавленных пользователей {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User newUser) {
        validateUser(newUser);
        newUser.setId(getNextUserId());
        if (newUser.getName() == null || newUser.getName().isEmpty()) {
            newUser.setName(newUser.getLogin());
        }
        users.put(newUser.getId(), newUser);
        log.info("Пользователь добавлен: {}", newUser);
        return newUser;
    }

    @Override
    public User updateUser(User updatedUser) {
        if (users.containsKey(updatedUser.getId())) {
            validateUser(updatedUser);
            User oldUser = users.get(updatedUser.getId());
            oldUser.setEmail(updatedUser.getEmail());
            if (updatedUser.getName() == null || updatedUser.getName().isEmpty()) {
                oldUser.setName(updatedUser.getLogin());
            } else {
                oldUser.setName(updatedUser.getName());
            }
            oldUser.setBirthday(updatedUser.getBirthday());
            oldUser.setLogin(updatedUser.getLogin());
            log.info("Пользователь обновлен: {}", updatedUser);
            return oldUser;
        }
        log.error("При попытке обновления пользователя не был найден его id: {}", updatedUser.getId());
        throw new NotFoundException("Пользователь с id = " + updatedUser.getId() + " не найден");
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь по указанному ID не найден");
        }
        return users.get(id);
    }

    @Override
    public void checkIdForUser(Long userId) {
        Optional<User> maybeUser = Optional.ofNullable(getUserById(userId));
        if (maybeUser.isEmpty()) {
            log.error("По Id {} не закреплено пользователя", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private long getNextUserId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    protected void validateUser(User user) {
        for (User u : users.values()) {
            if (u.getEmail().equals(user.getEmail())) {
                log.error("При попытке добавления/обновления пользователя указан существующий email: {}", u.getEmail());
                throw new ValidationException("Этот email уже используется");
            }
        }
    }
}
