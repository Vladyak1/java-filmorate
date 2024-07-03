package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    @NotNull (groups = OnUpdate.class)
    Long id;
    @NotBlank
    String name;
}
