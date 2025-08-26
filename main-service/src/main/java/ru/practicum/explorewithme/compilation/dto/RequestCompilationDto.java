package ru.practicum.explorewithme.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCompilationDto {
    @NotBlank
    @Size(max = 50)
    private String title;
    private Boolean pinned;
    private Set<Long> events;
}
