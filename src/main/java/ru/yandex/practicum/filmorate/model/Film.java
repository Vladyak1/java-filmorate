package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название не должно быть пустым")
    private String name;

    @NotBlank(message = "Описание не должно быть пустым")
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата выхода фильма не должна быть пустой")
    private LocalDate releaseDate;

    @NotNull(message = "Длительность фильма не должна быть пустой")
    @Positive(message = "Продолжительность фильма не может быть отрицательным числом")
    private long duration;

    private Set<Long> likes = new HashSet<>();

    private Genre genre;

    private MPA mpa;

    private enum Genre {
        COMEDY,
        DRAMA,
        CARTOON,
        Thriller,
        DOCUMENTARY,
        ACTION
    }

    public enum MPA {
        G, // у фильма нет возрастных ограничений,
        PG, // детям рекомендуется смотреть фильм с родителями,
        PG_13, // детям до 13 лет просмотр не желателен,
        R, // лицам до 17 лет просматривать фильм можно только в присутствии взрослого,
        NC_17, // лицам до 18 лет просмотр запрещён.
    }
}
