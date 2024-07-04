package ru.yandex.practicum.filmorate.storage.recommendation;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface RecommendationStorage {
    List<Film> getRecommendations(final long userId);
}
