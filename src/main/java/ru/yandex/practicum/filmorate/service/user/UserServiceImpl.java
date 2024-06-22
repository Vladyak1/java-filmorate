package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDbStorage userDbStorage;
    private static final String USER_DOES_NOT_EXIST = "Такого пользователя не существует";

    @Override
    public User addUser(User user) {
        log.info("Создание пользователя {}", user);
        if (userDbStorage.getUsers().contains(user)) {
            log.warn("Такой пользователь уже существует");
            throw new ValidationException("Такой пользователь уже существует");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userDbStorage.addUser(user);
    }

    @Override
    public void delUser(long id) {
        log.info("Удаление пользователя с id: {}", id);
        try {
            userDbStorage.findUser(id);
        } catch (EmptyResultDataAccessException e) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }
        log.info("Пользователь с id: {} удален", id);
        userDbStorage.delUser(id);
    }

    @Override
    public void delAllUsers() {
        log.info("Список пользователей очищен");
        userDbStorage.delAllUsers();
    }

    @Override
    public User updUser(User user) {
        log.info("Обновление пользователя {}", user);
        try {
            userDbStorage.findUser(user.getId());
        } catch (EmptyResultDataAccessException e) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }
        return userDbStorage.updUser(user);
    }

    @Override
    public List<User> getUsers() {
        log.info("Текущее количествоо пользователей: {}", userDbStorage.getUsers().size());
        return userDbStorage.getUsers();
    }

    @Override
    public void addFriend(long id, long friendId) {
        if (id == friendId) {
            log.warn("Пользователь не может добавить сам себя в друзья");
            throw new ValidationException("Невозможно добавить себя в друзья");
        }
        try {
            userDbStorage.findUser(id);
            userDbStorage.findUser(friendId);
        } catch (EmptyResultDataAccessException e) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }
        log.info("Пользователь с id: {} добавил в друзья пользователя с id: {}", id, friendId);
        userDbStorage.addFriend(id, friendId);
    }

    @Override
    public void delFriend(long id, long friendId) {
        if (id == friendId) {
            log.warn("Пользователь не может удалить сам себя из друзей");
            throw new ValidationException("Невозможно удалить себя из друзей");
        }
        try {
            userDbStorage.findUser(id);
            userDbStorage.findUser(friendId);
        } catch (EmptyResultDataAccessException e) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }

        log.info("Пользователь с id: {} удалил из друзей пользователя с id: {}", id, friendId);
        userDbStorage.delFriend(id, friendId);
    }

    @Override
    public List<User> getFriends(long id) {
        try {
            userDbStorage.findUser(id);
        } catch (EmptyResultDataAccessException e) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }
        log.info("Получен список друзей пользователя с id: {}", id);
        return userDbStorage.getFriends(id);
    }

    @Override
    public List<User> getCommonFriends(long id, long friendId) {
        if (userDbStorage.findUser(id) == null || userDbStorage.findUser(friendId) == null) {
            log.warn(USER_DOES_NOT_EXIST);
            throw new NotFoundException(USER_DOES_NOT_EXIST);
        }
        log.info("Получен список общих друзей пользователей с id: {} и {}", id, friendId);
        return userDbStorage.getCommonFriends(id, friendId);
    }

    @Override
    public User getUser(long id) {
        log.info("Получен пользователь с id: {}", id);
        return userDbStorage.findUser(id);
    }
}