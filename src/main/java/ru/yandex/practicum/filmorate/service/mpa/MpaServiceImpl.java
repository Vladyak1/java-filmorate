package ru.yandex.practicum.filmorate.service.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {
    private final MpaStorage mpaStorage;

    @Override
    public Mpa getMpa(int id) {
        log.info("Получен возрастной ретинг по id: {}", id);
        return mpaStorage.getMpaById(id);
    }

    @Override
    public List<Mpa> getAllMpa() {
        log.info("Получен список рейтингов MPA");
        return mpaStorage.getAllMpa();
    }
}