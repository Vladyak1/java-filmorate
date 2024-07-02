package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Класс представляет модель отзыва на фильм в приложении.
 * Отзывы могут быть положительными и негативными и иметь рейтинг полезности.
 <p>
 * Содержит:
 * <ul>
 * <li>{@code reviewId} - уникальный идентификатор отзыва</li>
 * <li>{@code content} - текст отзыва на фильм от пользователей</li>
 * <li>{@code isPositive} - переменная булевского типа для показа типа отзыва (позитивный - true или негативный - false)</li>
 * <li>{@code userId} - уникальный идентификатор пользователя, который оставил этот отзыв</li>
 * <li>{@code filmId} - уникальный идентификатор фильма, к которому относится этот отзыв</li>
 * <li>{@code useful} - рейтинг полезности отзыва, изначально равен нулю и может увеличиваться или уменьшаться</li>
 * </ul>
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reviews {
    private long reviewId;
    private long useful;

    @NotNull(message = "Текст отзыва не может быть null")
    private String content;

    @NotNull(message = "Тип отзыва не может быть null")
    private Boolean isPositive;

    @NotNull(message = "Уникальный идентификатор пользователя не может быть null")
    private Long userId;

    @NotNull(message = "Уникальный идентификатор фильма не может быть null")
    private Long filmId;
}
