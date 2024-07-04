package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
