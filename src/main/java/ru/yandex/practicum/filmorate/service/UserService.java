package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Long id, Long friendId) {
        userStorage.checkIdForUser(id);
        userStorage.checkIdForUser(friendId);
        userStorage.getUserById(id).getFriends().add(friendId);
        userStorage.getUserById(friendId).getFriends().add(id);
        log.info("{} и {} теперь друзья", userStorage.getUserById(id), userStorage.getUserById(friendId));
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.checkIdForUser(id);
        userStorage.checkIdForUser(friendId);
        userStorage.getUserById(id).getFriends().remove(friendId);
        userStorage.getUserById(friendId).getFriends().remove(id);
        log.info("{} и {} больше не друзья", userStorage.getUserById(id), userStorage.getUserById(friendId));
    }

    public List<User> usersFriendlist(Long id) {
        User user = userStorage.getUserById(id);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> commonFriends(Long id, Long otherId) {
        Set<Long> allFriendsIdsOfUser = new HashSet<>(userStorage.getUserById(id).getFriends());
        allFriendsIdsOfUser.retainAll(userStorage.getUserById(otherId).getFriends());
        return allFriendsIdsOfUser.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}
