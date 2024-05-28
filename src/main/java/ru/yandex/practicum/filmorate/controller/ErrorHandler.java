package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectParameterException(final ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final RuntimeException e) {
        return new ErrorResponse(
                "Произошла непредвиденная ошибка."
        );
    }

//    Заготовка для 409 ошибки
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ErrorResponse handleDuplicatedData(final DuplicatedDataException e) {
//        return new ErrorResponse(e.getMessage());
//    }

//    Заготовка для 422 ошибки
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
//    public ErrorResponse handleConditionsNotMet(final ConditionsNotMetException e) {
//        return new ErrorResponse(e.getMessage());
//    }
}
