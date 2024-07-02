package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
@Validated
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getDirectors() {
        log.info("Get directors");
        return directorService.getDirectors();
    }

    @GetMapping("{id}")
    public Director getDirector(@PathVariable long id) {
        log.info("Get director {}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Create director {}", director);
        return directorService.createDirector(director);
    }

    @PutMapping
    @Validated({OnUpdate.class})
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Update director {}", director);
        return directorService.updateDirector(director);
    }

    @DeleteMapping("{id}")
    public void deleteDirector(@PathVariable long id) {
        log.info("Delete director {}", id);
        directorService.deleteDirector(id);
    }
}
