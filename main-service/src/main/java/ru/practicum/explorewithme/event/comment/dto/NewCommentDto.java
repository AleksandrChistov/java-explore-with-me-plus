package ru.practicum.explorewithme.event.comment.dto;

import jakarta.validation.constraints.NotBlank;
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
}
