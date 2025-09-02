package ru.practicum.explorewithme.event.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.explorewithme.event.comment.enums.Status;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UpdateCommentDto {
    @NotNull
    private final Status status;
}
