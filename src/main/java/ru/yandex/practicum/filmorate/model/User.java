package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NotBlank(message = "Емейл не должен быть пустым")
    @Email(message = "Недопустимый формат емейла")
    private String email;

    @NotBlank(message = "Логин не должен быть пустым")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не должна быть пустой")
    @Past(message = "Указанная дата для поля дня рождения ещё не наступила")
    private LocalDate birthday;
}
