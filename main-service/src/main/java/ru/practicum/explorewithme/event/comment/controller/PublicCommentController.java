package ru.practicum.explorewithme.event.comment.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.service.PublicCommentService;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
public class PublicCommentController {

    public static final String URL = "/admin/events";
    private final PublicCommentService publicCommentService;

    @GetMapping("/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCommentDto> getCommentsByEventId(
            @Positive @PathVariable Long eventId
    ) {
        return publicCommentService.getCommentsByEventId(eventId);
    }

    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCommentDto> getAllCommentsByEventIds(
            @NotNull @RequestParam("eventIds") List<Long> eventIds
    ) {
        return publicCommentService.getAllCommentsByEventIds(eventIds);
    }
}
