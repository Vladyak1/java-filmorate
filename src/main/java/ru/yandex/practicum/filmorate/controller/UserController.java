package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Количество добавленных пользователей {}", users.size());
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User newUser) {
        validateUser(newUser);
        newUser.setId(getNextUserId());
        if (newUser.getName() == null || newUser.getName().isEmpty()) {
            newUser.setName(newUser.getLogin());
        }
        users.put(newUser.getId(), newUser);
        log.info("Пользователь добавлен: {}", newUser);
        return newUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User updatedUser) {
        validateUser(updatedUser);
        if (users.containsKey(updatedUser.getId())) {
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
        throw new ConditionsNotMetException("Пользователь с id = " + updatedUser.getId() + " не найден");
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
