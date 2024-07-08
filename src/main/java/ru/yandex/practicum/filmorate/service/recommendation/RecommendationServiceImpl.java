package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final RecommendationStorage recommendationStorage;

    @Override
    public List<Film> getRecommendations(long userId) {
        log.info("Вызов метода getRecommendations() c userId = {}", userId);
        return Optional.ofNullable(recommendationStorage.getRecommendations(userId))
                .orElseThrow( () -> new NotFoundException("Пользователь с id = " + userId + " не найден "));
    }
}
