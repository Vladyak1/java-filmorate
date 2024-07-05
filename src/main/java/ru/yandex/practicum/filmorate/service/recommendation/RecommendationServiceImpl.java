package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final RecommendationStorage recommendationStorage;

    @Override
    public List<Film> getRecommendations(long userId) {
        log.info("Вызов метода getRecommendations() c userId = {}", userId);
        try {
            return recommendationStorage.getRecommendations(userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователя с id {} не существует", userId);
            throw new NotFoundException(String.format("Пользователь с id = %s не найден ", userId));
        }
    }
}
