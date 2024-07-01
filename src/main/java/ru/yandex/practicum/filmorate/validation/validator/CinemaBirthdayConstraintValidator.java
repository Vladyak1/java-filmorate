package ru.yandex.practicum.filmorate.validation.validator;

import ru.yandex.practicum.filmorate.validation.annotation.AfterCinemaBirthday;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class CinemaBirthdayConstraintValidator implements ConstraintValidator<AfterCinemaBirthday, LocalDate> {
    private static final LocalDate cinemaBirthday = LocalDate.of(1895, 12, 27);

    @Override
    public boolean isValid(@NotNull LocalDate data, ConstraintValidatorContext cvc) {
        return data.isAfter(cinemaBirthday);
    }
}