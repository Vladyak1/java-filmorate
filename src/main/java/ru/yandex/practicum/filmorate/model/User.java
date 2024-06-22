package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "В логине не могут находиться пробелы")
    private String login;

    @NotNull(message = "Не указанна дата рождения")
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @NotNull(message = "Электронная почта не может быть пустой")
    @Email(message = "Некорректная электронная почта")
    private String email;
}
