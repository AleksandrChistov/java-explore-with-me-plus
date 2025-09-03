package ru.practicum.explorewithme.event.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.service.PrivateCommentService;

@RestController
@Slf4j
@RequestMapping("/users/{userId}")
@AllArgsConstructor
public class PrivateCommentController {

    private final PrivateCommentService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/events/{eventId}/comments")
    public ResponseCommentDto create(@PathVariable @Positive Long userId,
                                     @PathVariable @Positive Long eventId,
                                     @RequestBody @Valid NewCommentDto dto) {
        log.info("Создание комментария ");
        return service.create(userId, eventId, dto);
    }

    @DeleteMapping("/comments/{commentId}")
    public void delete(@PathVariable @Positive Long userId,
                       @PathVariable @Positive Long commentId) {
        log.info("Удаление пользователем своего комментария.");
        service.delete(userId, commentId);
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseCommentDto patch(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId,
                                    @RequestBody @Valid NewCommentDto dto) {
        log.info("Изменение комментария автором.");
        return service.patch(userId, commentId, dto);
    }
}
