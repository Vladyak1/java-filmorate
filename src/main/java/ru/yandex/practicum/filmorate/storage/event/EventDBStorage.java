package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mapper.EventMapper;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventDBStorage implements EventStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final EventMapper eventMapper;

    @Override
    public List<Event> getEventsByUserId(long userId) {
        try {
            return jdbcTemplate.query("select * from events", eventMapper);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void addEvent(Event event) {
        String sql = "insert into events (user_id, entity_id, event_type, operation) " +
                "values (:userId, :entityId, :eventType, :operation)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", event.getUserId())
                .addValue("entityId", event.getEntityId())
                .addValue("eventType", event.getEventType().name())
                .addValue("operation", event.getOperation().name());
        jdbcTemplate.update(sql, params);
    }
}
