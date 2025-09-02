package ru.practicum.explorewithme.event.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @NotBlank
    private String text;
    @NotNull
    @Positive
    private Long eventId;
    @NotNull
    @Positive
    private Long authorId;
}
