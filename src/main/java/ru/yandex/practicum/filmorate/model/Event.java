package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@EqualsAndHashCode(of = "eventId")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class Event {
    long eventId;
    long userId;
    long entityId;
    EventType eventType;
    Operation operation;
    long timestamp;
}
