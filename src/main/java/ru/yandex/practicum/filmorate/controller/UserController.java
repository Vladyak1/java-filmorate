package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @GetMapping("/{id}")
    public User findUser(@PathVariable long id) {
        return userService.getUser(id);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.updUser(user);
    }

    @GetMapping
    public ArrayList<User> findAll() {
        return new ArrayList<>(userService.getUsers());
    }

    @DeleteMapping
    public void deleteAll() {
        userService.delAllUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        userService.delUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void delFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.delFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> findAllFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("{id}/feed")
    public List<Event> getUserFeed(@PathVariable long id) {
        return userService.getUserFeed(id);
    }
}
