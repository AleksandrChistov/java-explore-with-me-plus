package ru.practicum.explorewithme.compilation.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private Set<Long> events;
    private Boolean pinned;
    private String title;
}
