package ru.practicum.explorewithme.event.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.explorewithme.event.comment.enums.Status;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @NotNull
    private Status status;
}
