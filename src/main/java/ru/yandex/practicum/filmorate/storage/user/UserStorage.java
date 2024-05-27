package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {


    Collection<User> getAllUsers();

    User addUser(User film);

    User updateUser(User film);

    User getUserById(Long id);

    void checkIdForUser(Long userId);

}