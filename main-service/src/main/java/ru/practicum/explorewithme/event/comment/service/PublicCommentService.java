package ru.practicum.explorewithme.event.comment.service;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;

import java.util.List;

public interface PublicCommentService {
    List<ResponseCommentDto> getCommentsByEventId(@Positive Long eventId, Pageable pageable);

    List<ResponseCommentDto> getAllCommentsByEventIds(@NotNull List<Long> eventIds, Pageable pageable);
}
