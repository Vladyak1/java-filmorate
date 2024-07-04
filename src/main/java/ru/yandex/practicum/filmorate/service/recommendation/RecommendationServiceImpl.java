package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final UserDbStorage userDbStorage;
    private final RecommendationStorage recommendationStorage;

    @Override
    public List<Film> getRecommendations(final long userId) {
        log.info("Вызов метода recommendationServiceImpl.getRecommendations() c userId = {}", userId);
        checkUser(userId);
        return recommendationStorage.getRecommendations(userId);
    }

    private void checkUser(final long userId) {
        log.info("Вызов метода recommendationServiceImpl.checkUser() c userId = {}", userId);
        final User user = userDbStorage.findUser(userId);
        Optional.ofNullable(user)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %s не найден ", userId)));
    }
}
